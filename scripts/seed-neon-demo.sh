#!/usr/bin/env bash

set -Eeuo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
RUN_ID="${RUN_ID:-$(date -u +%Y%m%d%H%M%S)}"
DEMO_PASSWORD="${DEMO_PASSWORD:-}"
REPORT_DIR="${REPORT_DIR:-.seed-reports}"
MODE="${MODE:-seed}"
RUN_SLUG="$(printf '%s' "$RUN_ID" | tr -cd '[:alnum:]' | tr '[:upper:]' '[:lower:]' | cut -c1-10)"
REPORT_PATH="${REPORT_DIR}/neon-demo-${RUN_SLUG}.json"

if [[ "$MODE" != "seed" && "$MODE" != "verify" ]]; then
  printf 'MODE must be either seed or verify.\n' >&2
  exit 1
fi

if [[ -z "$DEMO_PASSWORD" ]]; then
  printf 'DEMO_PASSWORD must be set and contain at least 8 characters.\n' >&2
  exit 1
fi

if (( ${#DEMO_PASSWORD} < 8 )); then
  printf 'DEMO_PASSWORD must contain at least 8 characters.\n' >&2
  exit 1
fi

for command in curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    printf 'Required command not found: %s\n' "$command" >&2
    exit 1
  fi
done

mkdir -p "$REPORT_DIR"
work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT

request() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local response_file="$work_dir/response.json"
  local args=(
    --silent
    --show-error
    --output "$response_file"
    --write-out '%{http_code}'
    --request "$method"
    "${API_BASE_URL}${path}"
    --header 'Accept: application/json'
  )

  if [[ -n "$token" ]]; then
    args+=(--header "Authorization: Bearer ${token}")
  fi

  if [[ -n "$body" ]]; then
    args+=(--header 'Content-Type: application/json' --data "$body")
  fi

  local status
  status="$(curl "${args[@]}")"

  if [[ "$status" -lt 200 || "$status" -ge 300 ]]; then
    printf 'Request failed: %s %s (HTTP %s)\n' "$method" "$path" "$status" >&2
    if [[ -s "$response_file" ]]; then
      jq . "$response_file" >&2 2>/dev/null || cat "$response_file" >&2
    fi
    exit 1
  fi

  if [[ ! -s "$response_file" ]]; then
    printf '{}'
    return
  fi

  cat "$response_file"
}

require_json_value() {
  local json="$1"
  local filter="$2"
  local label="$3"
  local value
  value="$(jq -er "$filter" <<<"$json")" || {
    printf 'Missing %s in API response.\n' "$label" >&2
    jq . <<<"$json" >&2
    exit 1
  }
  printf '%s' "$value"
}

printf 'Checking API health at %s...\n' "$API_BASE_URL"
health="$(request GET '/actuator/health')"
if [[ "$(jq -r '.status // empty' <<<"$health")" != "UP" ]]; then
  printf 'API health check did not report UP.\n' >&2
  jq . <<<"$health" >&2
  exit 1
fi

declare -a user_ids=()
declare -a usernames=()
declare -a emails=()
declare -a tokens=()
declare -a project_ids=()
declare -a comment_ids=()

if [[ "$MODE" == "seed" ]]; then
  printf 'Creating 10 demo users for run %s...\n' "$RUN_SLUG"
else
  printf 'Loading 10 existing demo users for run %s...\n' "$RUN_SLUG"
fi

for index in $(seq 1 10); do
  suffix="$(printf '%02d' "$index")"
  username="df${RUN_SLUG}u${suffix}"
  username="${username:0:20}"
  email="${username}@example.com"
  if [[ "$MODE" == "seed" ]]; then
    payload="$(jq -n \
      --arg email "$email" \
      --arg password "$DEMO_PASSWORD" \
      --arg username "$username" \
      --arg bio "DevForge Neon demo user ${suffix} from run ${RUN_SLUG}." \
      --arg location "Neon Demo Lab" \
      '{
        email: $email,
        password: $password,
        userName: $username,
        bio: $bio,
        location: $location,
        isPrivate: false,
        skills: ["Java", "PostgreSQL", "React"],
        interests: ["BACKEND", "CLOUD_COMPUTING"]
      }')"

    signup_response="$(request POST '/auth/signup' '' "$payload")"
    signup_user_id="$(require_json_value "$signup_response" '.data.id' 'signup user ID')"
  fi

  login_payload="$(jq -n --arg email "$email" --arg password "$DEMO_PASSWORD" \
    '{email: $email, password: $password}')"
  login_response="$(request POST '/auth/login' '' "$login_payload")"
  user_id="$(require_json_value "$login_response" '.data.userId' 'login user ID')"
  token="$(require_json_value "$login_response" '.data.jwt' 'login JWT')"

  if [[ "$MODE" == "seed" && "$signup_user_id" != "$user_id" ]]; then
    printf 'Signup/login user ID mismatch for %s.\n' "$username" >&2
    exit 1
  fi

  request GET "/users/${user_id}" "$token" >/dev/null
  user_ids+=("$user_id")
  usernames+=("$username")
  emails+=("$email")
  tokens+=("$token")
done

community_name="Neon Builders ${RUN_SLUG}"
if [[ "$MODE" == "seed" ]]; then
  community_payload="$(jq -n \
    --arg name "$community_name" \
    --arg description "Persistent DevForge demo community for Neon verification run ${RUN_SLUG}." \
    '{
      name: $name,
      description: $description,
      privacy: "PUBLIC"
    }')"
  community_response="$(request POST "/users/${user_ids[0]}/communities" "${tokens[0]}" "$community_payload")"
  community_id="$(require_json_value "$community_response" '.data.id' 'community ID')"

  printf 'Joining users to community %s...\n' "$community_id"
  for index in $(seq 1 9); do
    request POST "/users/${user_ids[$index]}/communities/${community_id}/join" "${tokens[$index]}" >/dev/null
  done

  printf 'Creating projects...\n'
  for index in $(seq 0 9); do
    number=$((index + 1))
    if (( index < 5 )); then
      visibility="$(jq -n '{isPublic: true}')"
    else
      visibility="$(jq -n --argjson communityId "$community_id" \
        '{isPublic: false, communityId: $communityId}')"
    fi

    project_payload="$(jq -n \
      --arg title "Neon Demo Project ${number} - ${RUN_SLUG}" \
      --arg description "A valid project created through the DevForge API to verify Neon persistence and authenticated activity." \
      --arg github "https://github.com/example/devforge-neon-demo-${number}" \
      --arg demo "https://example.com/devforge-neon-demo-${number}" \
      --argjson visibility "$visibility" \
      '{
        title: $title,
        description: $description,
        githubLink: $github,
        liveDemoLink: $demo,
        photos: [],
        techStacks: ["Java", "Spring Boot", "PostgreSQL"]
      } + $visibility')"

    project_response="$(request POST "/users/${user_ids[$index]}/projects" "${tokens[$index]}" "$project_payload")"
    project_id="$(require_json_value "$project_response" '.data.id' 'project ID')"
    project_ids+=("$project_id")
  done

  printf 'Creating follows, likes, bookmarks, and comments...\n'
  for index in $(seq 0 9); do
    target_index=$(((index + 1) % 10))
    target_user_id="${user_ids[$target_index]}"
    target_project_id="${project_ids[$target_index]}"
    token="${tokens[$index]}"

    follow_payload="$(jq -n --argjson followingId "$target_user_id" '{followingId: $followingId}')"
    request POST '/follows' "$token" "$follow_payload" >/dev/null
    request POST "/projects/${target_project_id}/likes" "$token" >/dev/null
    request POST "/bookmarks/bookmark?projectId=${target_project_id}" "$token" >/dev/null

    comment_payload="$(jq -n \
      --argjson projectId "$target_project_id" \
      --arg content "Demo feedback from ${usernames[$index]} during Neon run ${RUN_SLUG}." \
      '{projectId: $projectId, content: $content}')"
    comment_response="$(request POST '/comments' "$token" "$comment_payload")"
    comment_id="$(require_json_value "$comment_response" '.data.id' 'comment ID')"
    comment_ids+=("$comment_id")
  done

  printf 'Creating comment replies...\n'
  for index in $(seq 0 2); do
    replier_index=$(((index + 2) % 10))
    reply_payload="$(jq -n \
      --arg content "Reply from ${usernames[$replier_index]} for Neon run ${RUN_SLUG}." \
      '{content: $content}')"
    request POST "/comments/${comment_ids[$index]}/reply" "${tokens[$replier_index]}" "$reply_payload" >/dev/null
  done
else
  communities_response="$(request GET '/communities?page=0&size=100' "${tokens[0]}")"
  community_id="$(jq -er --arg name "$community_name" \
    '.data.content[] | select(.name == $name) | .id' <<<"$communities_response")" || {
    printf 'Could not find demo community %s.\n' "$community_name" >&2
    exit 1
  }

  for index in $(seq 0 9); do
    number=$((index + 1))
    title="Neon Demo Project ${number} - ${RUN_SLUG}"
    projects_response="$(request GET "/projects?userId=${user_ids[$index]}&page=0&size=100" "${tokens[$index]}")"
    project_id="$(jq -er --arg title "$title" \
      '.data.content[] | select(.title == $title) | .id' <<<"$projects_response")" || {
      printf 'Could not find demo project %s.\n' "$title" >&2
      exit 1
    }
    project_ids+=("$project_id")
  done
fi

printf 'Verifying activity through APIs...\n'
members_response="$(request GET "/communities/${community_id}/members?page=0&size=20" "${tokens[0]}")"
member_count="$(require_json_value "$members_response" '.data.totalElements' 'community member count')"
community_posts_response="$(request GET "/${community_id}/posts?page=0&size=20" "${tokens[0]}")"
community_project_count="$(require_json_value "$community_posts_response" '.data.totalElements' 'community project count')"
feed_response="$(request GET '/feed?page=0&size=30' "${tokens[0]}")"
feed_count="$(require_json_value "$feed_response" '.data.totalElements' 'feed project count')"
following_feed_response="$(request GET '/feed/following?page=0&size=20' "${tokens[0]}")"
following_feed_count="$(require_json_value "$following_feed_response" '.data.totalElements' 'following feed count')"

total_likes=0
total_bookmarks=0
total_followings=0
total_root_comments=0
total_replies=0
comment_ids=()

for index in $(seq 0 9); do
  liked_response="$(request GET "/users/${user_ids[$index]}/likes" "${tokens[$index]}")"
  bookmarked_response="$(request GET "/bookmarks/${user_ids[$index]}/recent" "${tokens[$index]}")"
  following_response="$(request GET "/follows/${user_ids[$index]}/followings?page=0&size=20" "${tokens[$index]}")"
  comments_response="$(request GET "/comments/projects/${project_ids[$index]}?page=0&size=20" "${tokens[$index]}")"

  total_likes=$((total_likes + $(jq '.data | length' <<<"$liked_response")))
  total_bookmarks=$((total_bookmarks + $(jq '.data | length' <<<"$bookmarked_response")))
  total_followings=$((total_followings + $(jq '.data.totalElements' <<<"$following_response")))
  total_root_comments=$((total_root_comments + $(jq '.data.totalElements' <<<"$comments_response")))
  total_replies=$((total_replies + $(jq '[.data.content[].replies | length] | add // 0' <<<"$comments_response")))
  comment_id="$(jq -er '.data.content[0].id' <<<"$comments_response")"
  comment_ids+=("$comment_id")
done

if (( member_count < 10 || community_project_count < 5 || following_feed_count < 1 )); then
  printf 'Community or following-feed verification failed.\n' >&2
  exit 1
fi

if (( total_likes < 10 || total_bookmarks < 10 || total_followings < 10 || total_root_comments < 10 || total_replies < 3 )); then
  printf 'Activity verification failed: likes=%d bookmarks=%d follows=%d comments=%d replies=%d\n' \
    "$total_likes" "$total_bookmarks" "$total_followings" "$total_root_comments" "$total_replies" >&2
  exit 1
fi

users_json='[]'
projects_json='[]'
comments_json='[]'
for index in $(seq 0 9); do
  users_json="$(jq \
    --argjson id "${user_ids[$index]}" \
    --arg username "${usernames[$index]}" \
    --arg email "${emails[$index]}" \
    '. + [{id: $id, userName: $username, email: $email}]' <<<"$users_json")"
  projects_json="$(jq --argjson id "${project_ids[$index]}" '. + [$id]' <<<"$projects_json")"
  comments_json="$(jq --argjson id "${comment_ids[$index]}" '. + [$id]' <<<"$comments_json")"
done

jq -n \
  --arg runId "$RUN_SLUG" \
  --arg apiBaseUrl "$API_BASE_URL" \
  --arg createdAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --argjson users "$users_json" \
  --argjson projectIds "$projects_json" \
  --argjson commentIds "$comments_json" \
  --argjson communityId "$community_id" \
  --argjson communityMembers "$member_count" \
  --argjson communityProjects "$community_project_count" \
  --argjson feedProjects "$feed_count" \
  --argjson followingFeedProjects "$following_feed_count" \
  --argjson likes "$total_likes" \
  --argjson bookmarks "$total_bookmarks" \
  --argjson follows "$total_followings" \
  --argjson rootComments "$total_root_comments" \
  --argjson replies "$total_replies" \
  '{
    runId: $runId,
    apiBaseUrl: $apiBaseUrl,
    createdAt: $createdAt,
    users: $users,
    projectIds: $projectIds,
    commentIds: $commentIds,
    communityId: $communityId,
    verification: {
      communityMembers: $communityMembers,
      communityProjects: $communityProjects,
      feedProjects: $feedProjects,
      followingFeedProjects: $followingFeedProjects,
      likes: $likes,
      bookmarks: $bookmarks,
      follows: $follows,
      rootComments: $rootComments,
      replies: $replies
    }
  }' >"$REPORT_PATH"

printf 'Neon demo data created and verified successfully.\n'
printf 'Report: %s\n' "$REPORT_PATH"
jq '{runId, communityId, userCount: (.users | length), projectCount: (.projectIds | length), verification}' "$REPORT_PATH"
