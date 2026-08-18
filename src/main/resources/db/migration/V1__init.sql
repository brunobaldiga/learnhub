create table users
(
    id          serial primary key,
    username    varchar(255) not null,
    email       varchar(255) not null,
    full_name   varchar(255) not null,
    password    varchar(255) not null,
    role_type   varchar(20)  not null,
    keycloak_id varchar(255),
    created_at  timestamp(6) not null
);

create unique index uk_users_username_lower on users (lower(username));
create unique index uk_users_email_lower on users (lower(email));

create table courses
(
    id             serial primary key,
    user_id        integer          not null,
    title          varchar(255)     not null,
    status         varchar(20)      not null,
    price          numeric(38, 2)   not null,
    sales_amount   integer          not null,
    average_rating double precision not null default 0,
    total_reviews  integer          not null default 0,
    created_at     timestamp(6)     not null,

    constraint fk_course_user foreign key (user_id) references users (id)
);

create table enrollments
(
    id          serial primary key,
    user_id     integer      not null,
    course_id   integer      not null,
    enrolled_at timestamp(6) not null,

    constraint fk_enrollment_user foreign key (user_id) references users (id),
    constraint fk_enrollment_course foreign key (course_id) references courses (id) on delete cascade,
    constraint uk_enrollment_user_course unique (user_id, course_id)
);

create table course_reviews
(
    id         serial primary key,
    course_id  integer      not null,
    author_id  integer      not null,
    rating     integer      not null,
    comment    varchar(500) not null,
    created_at timestamp(6) not null,

    constraint fk_course_review_course foreign key (course_id) references courses (id) on delete cascade,
    constraint fk_course_review_author foreign key (author_id) references users (id) on delete cascade,
    constraint chk_course_review_rating check (rating between 1 and 5),
    constraint uk_course_review_author_course unique (author_id, course_id)
);

create index idx_course_reviews_course_id on course_reviews (course_id);
create index idx_course_reviews_author_id on course_reviews (author_id);
create index idx_course_reviews_created_at on course_reviews (created_at);

create table payments
(
    id           serial primary key,
    user_id      integer        not null,
    course_id    integer        not null,
    course_title varchar(255)   not null,
    course_price numeric(38, 2) not null,
    currency     varchar(50)    not null,
    created_at   timestamp(6)   not null,

    constraint fk_payment_user foreign key (user_id) references users (id),
    constraint fk_payment_course foreign key (course_id) references courses (id),
    constraint uk_payment_user_course unique (user_id, course_id)
);

create index idx_payment_user_id on payments (user_id);
create index idx_payments_course_id on payments (course_id);
create index idx_payments_created_at on payments (created_at);

create table sections
(
    id         serial primary key,
    title      varchar(255) not null,
    position   integer      not null,
    course_id  integer      not null,
    created_at timestamp(6) not null,

    constraint fk_section_course foreign key (course_id) references courses (id) on delete cascade,
    constraint uk_section_course_index unique (course_id, position)
);

create table lessons
(
    id          serial primary key,
    section_id  integer       not null,
    content_url varchar(1000) not null,
    duration    integer       not null,
    position    integer       not null,
    created_at  timestamp(6)  not null,

    constraint fk_lesson_section foreign key (section_id) references sections (id) on delete cascade,
    constraint chk_lesson_duration check (duration > 0),
    constraint uk_lesson_section_index unique (section_id, position)
);

create table lesson_progress
(
    id                       serial primary key,
    enrollment_id            integer      not null,
    lesson_id                integer      not null,
    completed                boolean      not null default false,
    last_position_in_seconds integer      not null default 0,
    created_at               timestamp(6) not null,
    updated_at               timestamp(6) not null,

    constraint fk_lesson_progress_enrollment foreign key (enrollment_id) references enrollments (id) on delete cascade,
    constraint fk_lesson_progress_lesson foreign key (lesson_id) references lessons (id) on delete cascade,
    constraint uk_lesson_progress_enrollment_lesson unique (enrollment_id, lesson_id),
    constraint chk_last_position check (last_position_in_seconds >= 0)
);

create index idx_lesson_progress_enrollment on lesson_progress (enrollment_id);
create index idx_lesson_progress_lesson on lesson_progress (lesson_id);
create index idx_course_user on courses (user_id);

create index idx_section_course on sections (course_id);
create index idx_lessons_section on lessons (section_id);
create index idx_enrollments_user on enrollments (user_id);
create index idx_enrollments_course on enrollments (course_id);

create table certificates
(
    id                                 uuid primary key,
    enrollment_id                      integer      not null,
    full_name_at_issuance              varchar(255) not null,
    course_title_at_issuance           varchar(255) not null,
    course_length_in_hours_at_issuance integer      not null,
    issued_at                          date         not null,

    constraint fk_certificate_enrollment foreign key (enrollment_id) references enrollments (id) on delete cascade,
    constraint uk_certificate_enrollment unique (enrollment_id)
);

create index idx_certificates_enrollment on certificates (enrollment_id);

insert into users (username, email, full_name, password, role_type, keycloak_id, created_at)
values ('user',
        'user@learnhub.com',
        'John Doe',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'USER',
        NULL,
        CURRENT_TIMESTAMP),

       ('creator',
        'creator@learnhub.com',
        'Jane Doe',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'CREATOR',
        NULL,
        CURRENT_TIMESTAMP),
       ('admin',
        'admin@learnhub.com',
        'Admin',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7x6TSGT.bVfVki71RJKVQ1BM8DT8e',
        'ADMIN',
        NULL,
        CURRENT_TIMESTAMP);


insert into courses (user_id, title, status, price, sales_amount, average_rating, total_reviews, created_at)
values (2,
        'Spring Boot Masterclass',
        'PUBLIC',
        99.99,
        15,
        0,
        0,
        CURRENT_TIMESTAMP),
       (2,
        'React Fundamentals',
        'PUBLIC',
        79.99,
        8,
        0,
        0,
        CURRENT_TIMESTAMP);

insert into sections (title, position, course_id, created_at)
values ('Introduction',
        1,
        1,
        CURRENT_TIMESTAMP),
       ('Spring Core',
        2,
        1,
        CURRENT_TIMESTAMP),
       ('REST APIs',
        3,
        1,
        CURRENT_TIMESTAMP),
       ('Getting Started',
        1,
        2,
        CURRENT_TIMESTAMP),
       ('Components',
        2,
        2,
        CURRENT_TIMESTAMP),
       ('State Management',
        3,
        2,
        CURRENT_TIMESTAMP);

insert into lessons (section_id, content_url, duration, position, created_at)
values (1, 'https://www.youtube.com/watch?v=9SGDpanrc8U', 480, 1, CURRENT_TIMESTAMP),
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