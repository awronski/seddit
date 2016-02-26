# --- !Ups

CREATE TABLE comments (
    id bigint NOT NULL,
    parentid bigint,
    topparentid bigint,
    postid bigint NOT NULL,
    userid bigint,
    guest character varying(64),
    body text NOT NULL,
    created timestamp without time zone NOT NULL,
    ip character varying(15) NOT NULL
);
ALTER TABLE comments OWNER TO ali;

CREATE SEQUENCE comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE comments_id_seq OWNER TO ali;
ALTER SEQUENCE comments_id_seq OWNED BY comments.id;


CREATE TABLE posts (
    id bigint NOT NULL,
    link character varying(512) NOT NULL,
    userid bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    title character varying(160) NOT NULL,
    body text NOT NULL,
    pictures integer NOT NULL,
    votes integer NOT NULL,
    tags character varying(160)[] NOT NULL,
    visible boolean NOT NULL,
    commentsnumber integer NOT NULL
);
ALTER TABLE posts OWNER TO ali;

CREATE TABLE posts_votes (
    id bigint NOT NULL,
    postid bigint NOT NULL,
    userid bigint,
    vote integer NOT NULL,
    ip character varying(15) NOT NULL
);
ALTER TABLE posts_votes OWNER TO ali;
CREATE SEQUENCE posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE posts_id_seq OWNER TO ali;
ALTER SEQUENCE posts_id_seq OWNED BY posts.id;

CREATE SEQUENCE posts_votes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE posts_votes_id_seq OWNER TO ali;
ALTER SEQUENCE posts_votes_id_seq OWNED BY posts_votes.id;


CREATE TABLE users (
    id bigint NOT NULL,
    nick character varying(64) NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(60) NOT NULL,
    created timestamp without time zone NOT NULL,
    role character varying(16) NOT NULL,
    sex character(1) NOT NULL
);
ALTER TABLE users OWNER TO ali;
CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE users_id_seq OWNER TO ali;
ALTER SEQUENCE users_id_seq OWNED BY users.id;


ALTER TABLE ONLY comments ALTER COLUMN id SET DEFAULT nextval('comments_id_seq'::regclass);
ALTER TABLE ONLY posts ALTER COLUMN id SET DEFAULT nextval('posts_id_seq'::regclass);
ALTER TABLE ONLY posts_votes ALTER COLUMN id SET DEFAULT nextval('posts_votes_id_seq'::regclass);
ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


ALTER TABLE ONLY comments ADD CONSTRAINT comments_pkey PRIMARY KEY (id);
ALTER TABLE ONLY posts ADD CONSTRAINT posts_pkey PRIMARY KEY (id);
ALTER TABLE ONLY posts_votes ADD CONSTRAINT posts_votes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY users ADD CONSTRAINT users_email_key UNIQUE (email);
ALTER TABLE ONLY users ADD CONSTRAINT users_nick_key UNIQUE (nick);
ALTER TABLE ONLY users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_parentid_fkey FOREIGN KEY (parentid) REFERENCES comments(id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_postid_fkey FOREIGN KEY (postid) REFERENCES posts(id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_topparentid_fkey FOREIGN KEY (topparentid) REFERENCES comments(id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_userid_fkey FOREIGN KEY (userid) REFERENCES users(id);
ALTER TABLE ONLY posts ADD CONSTRAINT posts_userid_fkey FOREIGN KEY (userid) REFERENCES users(id);
ALTER TABLE ONLY posts_votes ADD CONSTRAINT posts_votes_postid_fkey FOREIGN KEY (postid) REFERENCES posts(id);


# --- !Downs