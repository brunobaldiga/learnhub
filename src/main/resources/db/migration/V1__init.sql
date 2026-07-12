CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_type VARCHAR(255) NOT NULL,
    keycloak_id VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX uk_users_username_lower
ON users (LOWER(username));

CREATE UNIQUE INDEX uk_users_email_lower
ON users (LOWER(email));

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
    (1,'https://www.youtube.com/watch?v=9SGDpanrc8U',1,CURRENT_TIMESTAMP),
    (1,'https://www.youtube.com/watch?v=vtPkZShrvXQ',2,CURRENT_TIMESTAMP),
    (1,'https://www.youtube.com/watch?v=HGTJBPNC-Gw',3,CURRENT_TIMESTAMP),

    (2,'https://www.youtube.com/watch?v=35EQXmHKZYs',1,CURRENT_TIMESTAMP),
    (2,'https://www.youtube.com/watch?v=Kw4xJfR5L9k',2,CURRENT_TIMESTAMP),
    (2,'https://www.youtube.com/watch?v=4XTsAAHW_Tc',3,CURRENT_TIMESTAMP),

    (3,'https://www.youtube.com/watch?v=8SGI_XS5OPw',1,CURRENT_TIMESTAMP),
    (3,'https://www.youtube.com/watch?v=5PdEmeopJVQ',2,CURRENT_TIMESTAMP),
    (3,'https://www.youtube.com/watch?v=6oOq6X4bQ4A',3,CURRENT_TIMESTAMP),

    (4,'https://www.youtube.com/watch?v=4XTsAAHW_Tc',1,CURRENT_TIMESTAMP),
    (4,'https://www.youtube.com/watch?v=vtPkZShrvXQ',2,CURRENT_TIMESTAMP),
    (4,'https://www.youtube.com/watch?v=35EQXmHKZYs',3,CURRENT_TIMESTAMP),

    (5,'https://www.youtube.com/watch?v=HGTJBPNC-Gw',1,CURRENT_TIMESTAMP),
    (5,'https://www.youtube.com/watch?v=9SGDpanrc8U',2,CURRENT_TIMESTAMP),
    (5,'https://www.youtube.com/watch?v=5PdEmeopJVQ',3,CURRENT_TIMESTAMP);