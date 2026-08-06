CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_type VARCHAR(20) NOT NULL,
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
    status VARCHAR(20) NOT NULL,
    price NUMERIC(38,2) NOT NULL,
    sales_amount INTEGER NOT NULL,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_course_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);

CREATE TABLE enrollments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    completed_lessons INTEGER NOT NULL DEFAULT 0,
    total_lessons INTEGER NOT NULL,
    enrolled_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_enrollment_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_enrollment_user_course
        UNIQUE(user_id, course_id)
);

CREATE TABLE course_reviews (
    id SERIAL PRIMARY KEY,
    course_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_course_review_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_course_review_author
        FOREIGN KEY (author_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_course_review_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT uk_course_review_author_course
        UNIQUE (author_id, course_id)
);

CREATE INDEX idx_course_reviews_course_id
    ON course_reviews(course_id);

CREATE INDEX idx_course_reviews_author_id
    ON course_reviews(author_id);

CREATE INDEX idx_course_reviews_created_at
    ON course_reviews(created_at);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    course_price NUMERIC(38,2) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_payment_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id)
);

CREATE INDEX idx_payments_user_id
    ON payments(user_id);

CREATE INDEX idx_payments_course_id
    ON payments(course_id);

CREATE INDEX idx_payments_created_at
    ON payments(created_at);

CREATE TABLE sections (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_section_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_section_course_index
        UNIQUE(course_id, position)
);
CREATE TABLE lessons (
    id SERIAL PRIMARY KEY,
    section_id INTEGER NOT NULL,
    content_url VARCHAR(1000) NOT NULL,
    duration INTEGER NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_lesson_section
        FOREIGN KEY (section_id)
            REFERENCES sections(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_lesson_duration
        CHECK (duration > 0),

    CONSTRAINT uk_lesson_section_index
        UNIQUE(section_id, position)
);

CREATE TABLE lesson_progress (
    id SERIAL PRIMARY KEY,

    enrollment_id INTEGER NOT NULL,
    lesson_id INTEGER NOT NULL,

    last_position_in_seconds INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_lesson_progress_enrollment
        FOREIGN KEY (enrollment_id)
            REFERENCES enrollments(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_lesson_progress_lesson
        FOREIGN KEY (lesson_id)
            REFERENCES lessons(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_lesson_progress_enrollment_lesson
        UNIQUE(enrollment_id, lesson_id),

    CONSTRAINT chk_last_position
        CHECK (last_position_in_seconds >= 0)
);

CREATE INDEX idx_lesson_progress_enrollment
    ON lesson_progress(enrollment_id);

CREATE INDEX idx_lesson_progress_lesson
    ON lesson_progress(lesson_id);

CREATE INDEX idx_courses_user
    ON courses(user_id);

CREATE INDEX idx_sections_course
    ON sections(course_id);

CREATE INDEX idx_lessons_section
    ON lessons(section_id);

CREATE INDEX idx_enrollments_user
    ON enrollments(user_id);

CREATE INDEX idx_enrollments_course
    ON enrollments(course_id);

CREATE TABLE certificates (
    id UUID PRIMARY KEY,

    enrollment_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,

    full_name_at_issuance VARCHAR(255) NOT NULL,
    course_title_at_issuance VARCHAR(255) NOT NULL,
    course_length_in_hours_at_issuance INTEGER NOT NULL,

    issued_at DATE NOT NULL,

    CONSTRAINT fk_certificate_enrollment
        FOREIGN KEY (enrollment_id)
            REFERENCES enrollments(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_certificate_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_certificate_enrollment_user
        UNIQUE (enrollment_id, user_id)
);

CREATE INDEX idx_certificates_user
    ON certificates(user_id);

CREATE INDEX idx_certificates_enrollment
    ON certificates(enrollment_id);

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
    average_rating,
    total_reviews,
    created_at
)
VALUES
    (
        2,
        'Spring Boot Masterclass',
        'PUBLIC',
        99.99,
        15,
        0,
        0,
        CURRENT_TIMESTAMP
    ),
    (
        2,
        'React Fundamentals',
        'PUBLIC',
        79.99,
        8,
        0,
        0,
        CURRENT_TIMESTAMP
    );

INSERT INTO sections (
    title,
    position,
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

INSERT INTO lessons (
    section_id,
    content_url,
    duration,
    position,
    created_at
)
VALUES
    (1, 'https://www.youtube.com/watch?v=9SGDpanrc8U', 480, 1, CURRENT_TIMESTAMP),
    (1, 'https://www.youtube.com/watch?v=vtPkZShrvXQ', 720, 2, CURRENT_TIMESTAMP),
    (1, 'https://www.youtube.com/watch?v=HGTJBPNC-Gw', 540, 3, CURRENT_TIMESTAMP),

    (2, 'https://www.youtube.com/watch?v=35EQXmHKZYs', 900, 1, CURRENT_TIMESTAMP),
    (2, 'https://www.youtube.com/watch?v=Kw4xJfR5L9k', 840, 2, CURRENT_TIMESTAMP),
    (2, 'https://www.youtube.com/watch?v=4XTsAAHW_Tc', 660, 3, CURRENT_TIMESTAMP),

    (3, 'https://www.youtube.com/watch?v=8SGI_XS5OPw', 780, 1, CURRENT_TIMESTAMP),
    (3, 'https://www.youtube.com/watch?v=5PdEmeopJVQ', 600, 2, CURRENT_TIMESTAMP),
    (3, 'https://www.youtube.com/watch?v=6oOq6X4bQ4A', 960, 3, CURRENT_TIMESTAMP),

    (4, 'https://www.youtube.com/watch?v=4XTsAAHW_Tc', 660, 1, CURRENT_TIMESTAMP),
    (4, 'https://www.youtube.com/watch?v=vtPkZShrvXQ', 720, 2, CURRENT_TIMESTAMP),
    (4, 'https://www.youtube.com/watch?v=35EQXmHKZYs', 900, 3, CURRENT_TIMESTAMP),

    (5, 'https://www.youtube.com/watch?v=HGTJBPNC-Gw', 540, 1, CURRENT_TIMESTAMP),
    (5, 'https://www.youtube.com/watch?v=9SGDpanrc8U', 480, 2, CURRENT_TIMESTAMP),
    (5, 'https://www.youtube.com/watch?v=5PdEmeopJVQ', 600, 3, CURRENT_TIMESTAMP);