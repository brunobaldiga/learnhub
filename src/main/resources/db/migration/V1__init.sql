CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_type VARCHAR(255) NOT NULL,
    keycloak_id VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE courses (
     id SERIAL PRIMARY KEY,
     user_id INTEGER NOT NULL,
     title VARCHAR(255) NOT NULL,
     status VARCHAR(255) NOT NULL,
     price NUMERIC(38,2) NOT NULL,
     sales_amount INTEGER NOT NULL,
     created_at TIMESTAMP(6) NOT NULL,

     CONSTRAINT fk_course_user
         FOREIGN KEY (user_id)
             REFERENCES users(id)
);

CREATE TABLE sections (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    "index" INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_section_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id)
);

CREATE TABLE videos (
    id SERIAL PRIMARY KEY,
    section_id INTEGER NOT NULL,
    video_url VARCHAR(1000) NOT NULL,
    "index" INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_video_section
        FOREIGN KEY (section_id)
            REFERENCES sections(id)
);

INSERT INTO users (
    username,
    email,
    password,
    role_type,
    keycloak_id,
    created_at
)
VALUES
    (
        'user',
        'user@learnhub.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'USER',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'creator',
        'creator@learnhub.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'CREATOR',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'admin',
        'admin@learnhub.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'ADMIN',
        NULL,
        CURRENT_TIMESTAMP
    );

INSERT INTO courses (
    user_id,
    title,
    status,
    price,
    sales_amount,
    created_at
)
VALUES
    (
        2,
        'Spring Boot Masterclass',
        'PUBLIC',
        99.99,
        15,
        CURRENT_TIMESTAMP
    ),
    (
        2,
        'React Fundamentals',
        'PUBLIC',
        79.99,
        8,
        CURRENT_TIMESTAMP
    );

INSERT INTO sections (
    title,
    "index",
    course_id,
    created_at
)
VALUES
    (
        'Introduction',
        1,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        'Spring Core',
        2,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        'REST APIs',
        3,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        'Getting Started',
        1,
        2,
        CURRENT_TIMESTAMP
    ),
    (
        'Components',
        2,
        2,
        CURRENT_TIMESTAMP
    ),
    (
        'State Management',
        3,
        2,
        CURRENT_TIMESTAMP
    );

INSERT INTO videos (
    section_id,
    video_url,
    "index",
    created_at
)

VALUES
    (1, 'https://example.com/videos/spring-intro-1.mp4', 1, CURRENT_TIMESTAMP),
    (1, 'https://example.com/videos/spring-intro-2.mp4', 2, CURRENT_TIMESTAMP),

    (2, 'https://example.com/videos/spring-core-1.mp4', 1, CURRENT_TIMESTAMP),
    (2, 'https://example.com/videos/spring-core-2.mp4', 2, CURRENT_TIMESTAMP),

    (3, 'https://example.com/videos/rest-api-1.mp4', 1, CURRENT_TIMESTAMP),
    (3, 'https://example.com/videos/rest-api-2.mp4', 2, CURRENT_TIMESTAMP),

    (4, 'https://example.com/videos/react-start-1.mp4', 1, CURRENT_TIMESTAMP),
    (4, 'https://example.com/videos/react-start-2.mp4', 2, CURRENT_TIMESTAMP),

    (5, 'https://example.com/videos/react-components-1.mp4', 1, CURRENT_TIMESTAMP),
    (5, 'https://example.com/videos/react-components-2.mp4', 2, CURRENT_TIMESTAMP),

    (6, 'https://example.com/videos/react-state-1.mp4', 1, CURRENT_TIMESTAMP),
    (6, 'https://example.com/videos/react-state-2.mp4', 2, CURRENT_TIMESTAMP);