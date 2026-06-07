CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    profile_picture_url VARCHAR(255),
    bio VARCHAR(500),
    location VARCHAR(255),
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    following_count BIGINT,
    follower_count BIGINT
);

CREATE TABLE community (
    id BIGSERIAL PRIMARY KEY,
    community_name VARCHAR(255),
    description VARCHAR(255),
    logo_url VARCHAR(255),
    banner_url VARCHAR(255),
    privacy VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1290) NOT NULL,
    live_demo_link VARCHAR(255),
    github_link VARCHAR(255),
    community_id BIGINT REFERENCES community(id),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(255),
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    like_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    bookmark_count BIGINT NOT NULL DEFAULT 0,
    score DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    project_id BIGINT NOT NULL REFERENCES projects(id),
    parent_id BIGINT REFERENCES comments(id)
);

CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    project_id BIGINT NOT NULL REFERENCES projects(id),
    created_at TIMESTAMP,
    CONSTRAINT uk_likes_user_project UNIQUE (user_id, project_id)
);

CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    project_id BIGINT NOT NULL REFERENCES projects(id),
    created_at TIMESTAMP,
    CONSTRAINT uk_bookmarks_user_project UNIQUE (user_id, project_id)
);

CREATE TABLE user_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES app_user(id),
    following_id BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP,
    CONSTRAINT uk_follow_follower_following UNIQUE (follower_id, following_id)
);

CREATE TABLE community_member (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    community_id BIGINT NOT NULL REFERENCES community(id),
    role VARCHAR(255),
    joined_at TIMESTAMP,
    CONSTRAINT uk_community_member_user_community UNIQUE (user_id, community_id)
);

CREATE TABLE project_tech_stack (
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    tech VARCHAR(255)
);

CREATE TABLE project_photos (
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    photos_order INTEGER NOT NULL,
    photo_url VARCHAR(255),
    PRIMARY KEY (project_id, photos_order)
);

CREATE TABLE user_skills (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    skill VARCHAR(255)
);

CREATE TABLE user_interests (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    interest VARCHAR(255)
);

CREATE TABLE user_links (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    link_type VARCHAR(255)
);

CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_username ON app_user(user_name);
CREATE INDEX idx_project_user_created ON projects(user_id, created_at);
CREATE INDEX idx_project_community_created ON projects(community_id, created_at);
CREATE INDEX idx_project_created ON projects(created_at);
CREATE INDEX idx_project_score ON projects(score);
CREATE INDEX idx_project_title ON projects(title);
CREATE INDEX idx_comment_project_parent_created ON comments(project_id, parent_id, created_at);
CREATE INDEX idx_comment_user ON comments(user_id);
CREATE INDEX idx_community_name ON community(community_name);
CREATE INDEX idx_community_privacy ON community(privacy);
CREATE INDEX idx_community_member_community ON community_member(community_id);
CREATE INDEX idx_community_member_user ON community_member(user_id);
CREATE INDEX idx_follow_follower ON user_follow(follower_id);
CREATE INDEX idx_follow_following ON user_follow(following_id);
CREATE INDEX idx_like_user_created ON likes(user_id, created_at);
CREATE INDEX idx_like_project ON likes(project_id);
CREATE INDEX idx_bookmark_user_created ON bookmarks(user_id, created_at);
CREATE INDEX idx_bookmark_project ON bookmarks(project_id);
