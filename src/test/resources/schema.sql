CREATE SCHEMA IF NOT EXISTS ADMIN_OWNER;
SET SCHEMA_SEARCH_PATH ADMIN_OWNER, INFORMATION_SCHEMA, PUBLIC;

create table IF NOT EXISTS ADMIN_OWNER.ACTION
(
    ID     CHAR(26)           not null,
    CODE   VARCHAR2(255 characters) not null unique,
    NAME   VARCHAR2(255 characters) not null,
    STATUS NUMBER(1)             not null,
    PRIMARY KEY (ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER."GROUP"
(
    ID                 VARCHAR2(26 characters)  not null
        constraint GROUP_PK
            primary key,
    CODE               VARCHAR2(255 characters) not null,
    NAME               VARCHAR2(255 characters),
    STATUS             NUMBER(1)             not null,
    CREATED_BY         VARCHAR2(50 characters),
    CREATED_DATE       TIMESTAMP(6),
    LAST_MODIFIED_BY   VARCHAR2(50 characters),
    LAST_MODIFIED_DATE TIMESTAMP(6),
    CLIENT_ID          VARCHAR2(26 characters)  not null,
    constraint CLIENT_GROUP_UIDX
        unique (CLIENT_ID, CODE)
)
;

create table IF NOT EXISTS ADMIN_OWNER.GROUP_ROLE
(
    ROLE_ID  VARCHAR2(26 characters) not null,
    GROUP_ID VARCHAR2(26 characters) not null,
    constraint GROUP_ROLE_PK
        unique (GROUP_ID, ROLE_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.GROUP_USER
(
    USER_ID  VARCHAR2(26 characters) not null,
    GROUP_ID VARCHAR2(26 characters) not null,
    constraint GROUP_USER_PK
        unique (GROUP_ID, USER_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.OBJECT
(
    ID        CHAR(26)           not null
        constraint OBJECT_PK
            primary key,
    NAME      VARCHAR2(255 characters) not null,
    CODE      VARCHAR2(255 characters) not null,
    APP_ID    VARCHAR2(255 characters) not null,
    URI       VARCHAR2(50 characters),
    ICON      VARCHAR2(50 characters),
    PARENT_ID VARCHAR2(26 characters),
    STATUS    NUMBER             not null,
    ORDINAL   NUMBER,
    constraint OBJECT_APP_ID_CODE_PK
        unique (APP_ID, CODE)
)
;

create table IF NOT EXISTS ADMIN_OWNER.OBJECT_ACTION
(
    OBJECT_ID CHAR(26)            not null
        constraint OBJECT_ACTION_OBJECT_ID_FK
            references ADMIN_OWNER.OBJECT,
    ACTION_ID CHAR(26)            not null
        constraint OBJECT_ACTION___FK
            references ADMIN_OWNER.ACTION,
    ID        CHAR(26)            not null
        constraint OBJECT_ACTION_PK1
            primary key,
    NAME      VARCHAR2(100)       not null,
    STATUS    NUMBER(1) default 1 not null,
    constraint OBJECT_ACTION_PK unique (ACTION_ID, OBJECT_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.PARAM
(
    ID           VARCHAR2(26 characters)  not null
        constraint PARAM_PK
            primary key,
    TYPE         VARCHAR2(50 characters)  not null,
    CODE         VARCHAR2(100 characters) not null,
    "VALUE"        VARCHAR2(400 characters) not null,
    DESCRIPTION  VARCHAR2(1255 characters),
    STATUS       NUMBER(1)             not null,
    APPCODE      VARCHAR2(50 characters),
    TRANSLATIONS VARCHAR2(100 characters)
)
;

create table IF NOT EXISTS ADMIN_OWNER.ROLE
(
    ID                 VARCHAR2(26 characters)  not null
        constraint ROLE_PK primary key,
    CODE               VARCHAR2(100 characters) not null,
    NAME               VARCHAR2(100 characters),
    STATUS             NUMBER             not null,
    DESCRIPTION        VARCHAR2(255 characters),
    CREATED_BY         VARCHAR2(50 characters),
    CREATED_DATE       TIMESTAMP(6),
    LAST_MODIFIED_BY   VARCHAR2(50 characters),
    LAST_MODIFIED_DATE TIMESTAMP(6),
    CLIENT_ID          VARCHAR2(26 characters),
    APP_ID             VARCHAR2(100 characters) not null,
    constraint APP_ROLE_UK
        unique (APP_ID, CODE)
)
;

create table IF NOT EXISTS ADMIN_OWNER.ROLE_OBJECT_ACTION
(
    ROLE_ID   VARCHAR2(26 characters) not null,
    ACTION_ID VARCHAR2(26 characters) not null,
    OBJECT_ID VARCHAR2(26 characters) not null,
    constraint ROLE_OBJECT_ACTION_PK
        unique (ROLE_ID, OBJECT_ID, ACTION_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.ROLE_USER
(
    ROLE_ID VARCHAR2(26 characters) not null,
    USER_ID VARCHAR2(26 characters) not null,
    constraint ROLE_USER_PK
        unique (ROLE_ID, USER_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER."USER"
(
    ID                   VARCHAR2(26 characters)   not null
        constraint USER_PK
            primary key,
    USERNAME             VARCHAR2(255 characters)  not null,
    PASSWORD             VARCHAR2(255 characters),
    FULLNAME             VARCHAR2(255 characters),
    DATE_OF_BIRTH        DATE,
    POSITION_TITLE       VARCHAR2(255 characters),
    STATUS               NUMBER              not null,
    TYPE                 VARCHAR2(20 characters),
    ID_CARD_NO           VARCHAR2(12 characters),
    PHONE_NUMBER         VARCHAR2(20 characters),
    LOGIN_FAILED_COUNT   NUMBER    default 0,
    PASSWORD_EXPIRE_TIME TIMESTAMP(6),
    CREATED_BY           VARCHAR2(50 characters),
    CREATED_DATE         TIMESTAMP(6),
    LAST_MODIFIED_BY     VARCHAR2(50 characters),
    LAST_MODIFIED_DATE   TIMESTAMP(6),
    GENDER               NUMBER,
    CLIENT_ID            VARCHAR2(26 characters)   not null,
    EMAIL                VARCHAR2(255)       not null,
    LOGIN_METHOD         NUMBER(1) default 1 not null,
    constraint EMAIL_PK
        unique (CLIENT_ID, EMAIL),
    constraint USERNAME_PK
        unique (CLIENT_ID, USERNAME)
)
;

comment on column ADMIN_OWNER."USER".LOGIN_METHOD is 'Phương thức đăng nhập: 1 = Username, 2 = Google'
;

create table IF NOT EXISTS ADMIN_OWNER.OAUTH2_REGISTERED_CLIENT
(
    ID                            VARCHAR2(100 characters)                            not null
        primary key,
    CLIENT_ID                     VARCHAR2(100 characters)                            not null,
    CLIENT_ID_ISSUED_AT           TIMESTAMP(6)        default CURRENT_TIMESTAMP not null,
    CLIENT_SECRET                 VARCHAR2(200 characters)  default NULL,
    CLIENT_SECRET_EXPIRES_AT      TIMESTAMP(6)        default NULL,
    CLIENT_NAME                   VARCHAR2(200 characters)                            not null,
    CLIENT_AUTHENTICATION_METHODS VARCHAR2(1000 characters)                           not null,
    AUTHORIZATION_GRANT_TYPES     VARCHAR2(1000 characters)                           not null,
    REDIRECT_URIS                 VARCHAR2(1000 characters) default NULL,
    POST_LOGOUT_REDIRECT_URIS     VARCHAR2(1000 characters) default NULL,
    SCOPES                        VARCHAR2(1000 characters)                           not null,
    CLIENT_SETTINGS               VARCHAR2(2000 characters)                           not null,
    TOKEN_SETTINGS                VARCHAR2(2000 characters)                           not null
)
;

create table IF NOT EXISTS ADMIN_OWNER.OAUTH2_AUTHORIZATION_CONSENT
(
    REGISTERED_CLIENT_ID VARCHAR2(100 characters)  not null,
    PRINCIPAL_NAME       VARCHAR2(200 characters)  not null,
    AUTHORITIES          VARCHAR2(1000 characters) not null,
    primary key (REGISTERED_CLIENT_ID, PRINCIPAL_NAME)
)
;

create table IF NOT EXISTS ADMIN_OWNER.OAUTH2_AUTHORIZATION
(
    ID                            VARCHAR2(100 characters) not null
        primary key,
    REGISTERED_CLIENT_ID          VARCHAR2(100 characters) not null,
    PRINCIPAL_NAME                VARCHAR2(200 characters) not null,
    AUTHORIZATION_GRANT_TYPE      VARCHAR2(100 characters) not null,
    AUTHORIZED_SCOPES             VARCHAR2(1000 characters) default NULL,
    ATTRIBUTES                    CLOB                default NULL,
    STATE                         VARCHAR2(500 characters)  default NULL,
    AUTHORIZATION_CODE_VALUE      VARCHAR2(4000 characters) default NULL,
    AUTHORIZATION_CODE_ISSUED_AT  TIMESTAMP(6)        default NULL,
    AUTHORIZATION_CODE_EXPIRES_AT TIMESTAMP(6)        default NULL,
    AUTHORIZATION_CODE_METADATA   CLOB                default NULL,
    ACCESS_TOKEN_VALUE            VARCHAR2(4000 characters) default NULL,
    ACCESS_TOKEN_ISSUED_AT        TIMESTAMP(6)        default NULL,
    ACCESS_TOKEN_EXPIRES_AT       TIMESTAMP(6)        default NULL,
    ACCESS_TOKEN_METADATA         CLOB                default NULL,
    ACCESS_TOKEN_TYPE             VARCHAR2(100 characters)  default NULL,
    ACCESS_TOKEN_SCOPES           VARCHAR2(1000 characters) default NULL,
    OIDC_ID_TOKEN_VALUE           VARCHAR2(4000 characters) default NULL,
    OIDC_ID_TOKEN_ISSUED_AT       TIMESTAMP(6)        default NULL,
    OIDC_ID_TOKEN_EXPIRES_AT      TIMESTAMP(6)        default NULL,
    OIDC_ID_TOKEN_METADATA        CLOB                default NULL,
    REFRESH_TOKEN_VALUE           VARCHAR2(4000 characters) default NULL,
    REFRESH_TOKEN_ISSUED_AT       TIMESTAMP(6)        default NULL,
    REFRESH_TOKEN_EXPIRES_AT      TIMESTAMP(6)        default NULL,
    REFRESH_TOKEN_METADATA        CLOB                default NULL,
    USER_CODE_VALUE               VARCHAR2(4000 characters) default NULL,
    USER_CODE_ISSUED_AT           TIMESTAMP(6)        default NULL,
    USER_CODE_EXPIRES_AT          TIMESTAMP(6)        default NULL,
    USER_CODE_METADATA            CLOB                default NULL,
    DEVICE_CODE_VALUE             VARCHAR2(4000 characters) default NULL,
    DEVICE_CODE_ISSUED_AT         TIMESTAMP(6)        default NULL,
    DEVICE_CODE_EXPIRES_AT        TIMESTAMP(6)        default NULL,
    DEVICE_CODE_METADATA          CLOB                default NULL
)
;

create index IF NOT EXISTS ADMIN_OWNER.OAUTH2_AUTHORIZATION_REFRESH_TOKEN_VALUE_INDEX
    on ADMIN_OWNER.OAUTH2_AUTHORIZATION (REFRESH_TOKEN_VALUE)
;

create index IF NOT EXISTS ADMIN_OWNER.OAUTH2_AUTHORIZATION_PRINCIPAL_NAME_INDEX
    on ADMIN_OWNER.OAUTH2_AUTHORIZATION (PRINCIPAL_NAME)
;

create table IF NOT EXISTS ADMIN_OWNER.CLIENT
(
    ID                    VARCHAR2(26 characters)  not null
        primary key,
    CODE                  VARCHAR2(255 characters)
        constraint CLIENT_CODE_UNIQUE
            unique,
    NAME                  VARCHAR2(255 characters) not null,
    CONTACT_NAME          VARCHAR2(255 characters),
    CONTACT_POSITION      VARCHAR2(100 characters),
    CONTACT_PHONE         VARCHAR2(255 characters),
    CONTACT_EMAIL         VARCHAR2(255 characters),
    PERMANENT_ADDRESS     VARCHAR2(255 characters),
    PERMANENT_PROVINCE_ID NUMBER,
    PERMANENT_DISTRICT_ID NUMBER,
    PERMANENT_WARD_ID     NUMBER,
    TAX_CODE              VARCHAR2(50 characters),
    CREATED_BY            VARCHAR2(50 characters),
    CREATED_DATE          DATE,
    LAST_MODIFIED_BY      VARCHAR2(50 characters),
    LAST_MODIFIED_DATE    DATE,
    STATUS                NUMBER             not null
)
;

create table IF NOT EXISTS ADMIN_OWNER.FCM_USER_TOKEN
(
    ID               VARCHAR2(255 characters) not null
        constraint FCM_USER_TOKEN_PK
            primary key,
    CLIENT_ID        VARCHAR2(26 characters),
    USER_ID          VARCHAR2(26 characters),
    CREATED_TIME     DATE               not null,
    LAST_ACCESS_TIME DATE
)
;

create index IF NOT EXISTS ADMIN_OWNER.FCM_USER_TOKEN_CLIENT_ID_INDEX
    on ADMIN_OWNER.FCM_USER_TOKEN (CLIENT_ID, USER_ID)
;

create global temporary table if not exists ADMIN_OWNER.CURRENT_OBJECT_ACTION
(
    OBJECT_ID VARCHAR2(255),
    ACTION_ID VARCHAR2(255)
)
    NOT PERSISTENT
--     on commit delete rows
;

create table IF NOT EXISTS ADMIN_OWNER.DEPARTMENT_USER
(
    DEPARTMENT_ID NUMBER       not null,
    USER_ID       VARCHAR2(26) not null,
    constraint DEPARTMENT_USER_PK
        unique (DEPARTMENT_ID, USER_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.DEPARTMENT
(
    ID     NUMBER        not null
        constraint DEPARTMENT_PK
            primary key,
    NAME   VARCHAR2(100) not null,
    STATUS NUMBER        not null,
    CODE   VARCHAR2(50)  not null
)
;

create table IF NOT EXISTS ADMIN_OWNER.SYSTEM_AUDIT_LOG
(
    ID          VARCHAR2(26)  not null
        constraint SYSTEM_AUDIT_LOG_PK
            primary key,
    SUB_SYSTEM  VARCHAR2(20)  not null,
    ACTION_TYPE VARCHAR2(10)  not null,
    ACTION_TIME DATE          not null,
    CLIENT_ID   VARCHAR2(26)  not null,
    USER_ID     VARCHAR2(26)  not null,
    USERNAME    VARCHAR2(100) not null,
    FULLNAME    VARCHAR2(255),
    TARGET_TYPE VARCHAR2(50)  not null,
    PRE_VALUE   CLOB,
    POST_VALUE  CLOB,
    CLIENT_CODE VARCHAR2(100),
    CLIENT_NAME VARCHAR2(255),
    STATUS      NUMBER        not null,
    SITE_ID     VARCHAR2(100),
    SITE_CODE   VARCHAR2(100),
    SITE_NAME   VARCHAR2(200),
    CLIENT_IP   VARCHAR2(50)
)
;

create table IF NOT EXISTS ADMIN_OWNER.SYSTEM_ACCESS_LOG
(
    ID          VARCHAR2(26) not null
        constraint SYSTEM_ACCESS_LOG_PK
            primary key,
    ACCESS_TIME DATE         not null,
    ACTION_TYPE VARCHAR2(50) not null,
    CLIENT_ID   VARCHAR2(26) not null,
    CLIENT_CODE VARCHAR2(100),
    CLIENT_NAME VARCHAR2(255),
    USER_ID     VARCHAR2(26) not null,
    USERNAME    VARCHAR2(255 characters),
    FULLNAME    VARCHAR2(255),
    SITE_ID     VARCHAR2(100 characters),
    SITE_CODE   VARCHAR2(100 characters),
    SITE_NAME   VARCHAR2(200 characters),
    STATUS      NUMBER       not null,
    CLIENT_IP   VARCHAR2(50)
)
;

create table IF NOT EXISTS ADMIN_OWNER.TEST_JSON
(
    ID   CHAR(26)      not null
        constraint TEST_JSON_PK
            primary key,
    VAL json,
    NAME VARCHAR2(100) not null
)
;

create table IF NOT EXISTS ADMIN_OWNER.SYSTEM_AUDIT_DOMAIN
(
    CODE VARCHAR2(50)  not null
        constraint SYSTEM_AUDIT_DOMAIN_PK
            primary key,
    NAME VARCHAR2(100) not null
)
;

create table IF NOT EXISTS ADMIN_OWNER.API_CATALOG
(
    ID           CHAR(26)            not null
        constraint API_CATALOG_PK
            primary key,
    NAME         VARCHAR2(200)       not null,
    SERVICE_CODE VARCHAR2(20)        not null,
    URI_PATTERN  VARCHAR2(250)       not null,
    METHOD       VARCHAR2(10)        not null,
    STATUS       NUMBER(1) default 1 not null,
    constraint API_CATALOG_PK2
        unique (SERVICE_CODE, URI_PATTERN, METHOD)
)
;

comment on column ADMIN_OWNER.API_CATALOG.NAME is 'Tên gợi nhớ API'
;

comment on column ADMIN_OWNER.API_CATALOG.SERVICE_CODE is 'định danh service'
;

comment on column ADMIN_OWNER.API_CATALOG.URI_PATTERN is 'Uri pattern của API'
;

comment on column ADMIN_OWNER.API_CATALOG.METHOD is 'Method của API'
;

create table IF NOT EXISTS ADMIN_OWNER.API_GROUP
(
    ID     CHAR(26)            not null
        constraint API_GROUP_PK
            primary key,
    NAME   VARCHAR2(100)       not null,
    STATUS NUMBER(1) default 1 not null
)
;

comment on column ADMIN_OWNER.API_GROUP.NAME is 'Tên nhóm API'
;

create table IF NOT EXISTS ADMIN_OWNER.API_GROUP_ACL
(
    GROUP_ID CHAR(26) not null
        constraint API_GROUP_ACL___FK1
            references ADMIN_OWNER.API_GROUP,
    ACL_ID   CHAR(26) not null
        constraint API_GROUP_ACL___FK2
            references ADMIN_OWNER.OBJECT_ACTION,
    constraint API_GROUP_ACL_PK
        primary key (GROUP_ID, ACL_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.API_GROUP_CATALOG
(
    GROUP_ID   CHAR(26) not null
        constraint API_GROUP_CATALOG_API_GROUP_ID_FK
            references ADMIN_OWNER.API_GROUP,
    CATALOG_ID CHAR(26) not null
        constraint API_GROUP_CATALOG_API_CATALOG_ID_FK
            references ADMIN_OWNER.API_CATALOG,
    constraint API_GROUP_CATALOG_PK
        primary key (GROUP_ID, CATALOG_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.API_CATALOG_ACL
(
    CATALOG_ID CHAR(26) not null
        constraint API_CATALOG_ACL_API_CATALOG_ID_FK
            references ADMIN_OWNER.API_CATALOG,
    ACL_ID     CHAR(26) not null
        constraint API_CATALOG_ACL_OBJECT_ACTION_ID_FK
            references ADMIN_OWNER.OBJECT_ACTION,
    constraint API_CATALOG_ACL_PK
        primary key (CATALOG_ID, ACL_ID)
)
;

create table IF NOT EXISTS ADMIN_OWNER.NOTIFICATION
(
    ID          CHAR(26)           not null
        constraint NOTIFICATION_PK
            primary key,
    TITLE       VARCHAR2(200 characters) not null,
    CONTENT     CLOB               not null,
    SEND_DATE   DATE               not null,
    SEEN        NUMBER(1),
    URI_REF     VARCHAR2(200),
    PROPS       json default '{}',
    RECEIVER_ID CHAR(26)           not null
)
;

create index IF NOT EXISTS ADMIN_OWNER.NOTIFICATION_RECEIVER_ID_SEND_DATE_INDEX
    on ADMIN_OWNER.NOTIFICATION (RECEIVER_ID, SEND_DATE)
;

create table IF NOT EXISTS ADMIN_OWNER.SYSTEM_AUDIT_SITE_DOMAIN
(
    SITE_CODE   VARCHAR2(50) not null,
    DOMAIN_CODE VARCHAR2(50) not null,
    constraint SYSTEM_AUDIT_SITE_DOMAIN_PK
        unique (DOMAIN_CODE, SITE_CODE)
)
;

CREATE ALIAS IF NOT EXISTS GET_MENU_ITEM_HIERARCHY FOR "vn.vnsky.bcss.admin.proc.GetMenuItemHierarchy.call";

-- create PROCEDURE GET_MENU_ITEM_FLAT(P_USER_ID IN VARCHAR2, P_APP_CODE IN VARCHAR2, P_RC OUT SYS_REFCURSOR)
--     IS
--     V_APP_ID   VARCHAR2(100);
-- BEGIN
--     BEGIN
--         SELECT ID
--         INTO V_APP_ID
--         FROM OAUTH2_REGISTERED_CLIENT
--         WHERE CLIENT_ID = P_APP_CODE
--         OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;
--     EXCEPTION
--         WHEN NO_DATA_FOUND THEN
--             V_APP_ID := NULL;
--     END;
--     IF P_USER_ID IS NULL THEN
--         OPEN P_RC FOR
--             WITH CTE AS (
--                 SELECT ID, CODE, NAME, URI, ICON, PARENT_ID, 0 AS "LEVEL", ''  AS PATH
--                 FROM   OBJECT
--                 WHERE APP_ID = V_APP_ID AND STATUS = 1
--             ),
--                  RIGHTS AS (SELECT O.ID,
--                                    LISTAGG(DISTINCT A.CODE, ',') WITHIN GROUP (ORDER BY A.CODE) AS JOINED_ACTIONS
--                             FROM OBJECT O
--                                      INNER JOIN OBJECT_ACTION OA ON O.ID = OA.OBJECT_ID
--                                      INNER JOIN ACTION A ON OA.ACTION_ID = A.ID
--                             GROUP BY O.ID)
--             SELECT CTE.*, RIGHTS.JOINED_ACTIONS
--             FROM CTE LEFT JOIN RIGHTS ON CTE.ID = RIGHTS.ID
--             ORDER BY CTE.PATH
--         ;
--
--     ELSE
--         OPEN P_RC FOR
--             WITH CTE AS (
--                 SELECT ID, CODE, NAME, URI, ICON, PARENT_ID, 0 AS "LEVEL", ''  AS PATH
--                 FROM   OBJECT
--                 WHERE APP_ID = V_APP_ID AND STATUS = 1
--             ),
--                  MERGED_ROLE_USER AS (
--                      SELECT RU.ROLE_ID, RU.USER_ID FROM ROLE_USER RU WHERE RU.USER_ID = P_USER_ID
--                      UNION ALL
--                      SELECT RG.ROLE_ID, GU.USER_ID FROM GROUP_ROLE RG
--                                                             INNER JOIN GROUP_USER GU ON RG.GROUP_ID = GU.GROUP_ID
--                      WHERE GU.USER_ID = P_USER_ID
--                  ),
--                  RIGHTS AS (SELECT ROA.OBJECT_ID,
--                                    LISTAGG(DISTINCT A.CODE, ',') WITHIN GROUP (ORDER BY A.CODE) AS JOINED_ACTIONS
--                             FROM ROLE_OBJECT_ACTION ROA
--                                      INNER JOIN OBJECT O ON ROA.OBJECT_ID = O.ID
--                                      INNER JOIN ACTION A ON ROA.ACTION_ID = A.ID
--                                      INNER JOIN MERGED_ROLE_USER RU ON ROA.ROLE_ID = RU.ROLE_ID
--                                      INNER JOIN ROLE R ON ROA.ROLE_ID = R.ID
--                             WHERE R.STATUS = 1
--                             GROUP BY ROA.OBJECT_ID)
--             SELECT CTE.*, RIGHTS.JOINED_ACTIONS
--             FROM CTE LEFT JOIN RIGHTS ON CTE.ID = RIGHTS.OBJECT_ID
--             ORDER BY CTE.PATH;
--     END IF;
--
-- END;
-- ;
--
-- create PROCEDURE GET_PERMISSION_LIST(P_USER_ID IN VARCHAR2, P_APP_CODE IN VARCHAR2,
--                                      P_OWNER_TYPE IN VARCHAR2, P_RC OUT SYS_REFCURSOR)
--     IS
--     V_APP_ID VARCHAR2(100);
--     V_IS_OWNER NUMBER(1) := 0;
-- BEGIN
--     BEGIN
--         SELECT ID
--         INTO V_APP_ID
--         FROM OAUTH2_REGISTERED_CLIENT
--         WHERE CLIENT_ID = P_APP_CODE
--         OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;
--     EXCEPTION
--         WHEN NO_DATA_FOUND THEN
--             V_APP_ID := NULL;
--     END;
--     BEGIN
--         SELECT 1
--         INTO V_IS_OWNER
--         FROM "USER"
--         WHERE ID = P_USER_ID AND "TYPE" = P_OWNER_TYPE
--         OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;
--     EXCEPTION
--         WHEN NO_DATA_FOUND THEN
--             V_IS_OWNER := 0;
--     END;
--     IF V_IS_OWNER = 1 THEN
--         OPEN P_RC FOR
--             SELECT O.CODE AS OBJECT_CODE, A.CODE AS ACTION_CODE
--             FROM OBJECT_ACTION OA
--                      INNER JOIN OBJECT O ON OA.OBJECT_ID = O.ID
--                      INNER JOIN ACTION A ON OA.ACTION_ID = A.ID;
--
--     ELSE
--         OPEN P_RC FOR
--             WITH MERGED_ROLE_USER AS (SELECT NULL AS GROUP_ID, RU.ROLE_ID
--                                       FROM ROLE_USER RU
--                                                INNER JOIN ROLE R ON RU.ROLE_ID = R.ID
--                                       WHERE RU.USER_ID = P_USER_ID
--                                         AND R.STATUS = 1
--                                       UNION ALL
--                                       SELECT GU.GROUP_ID, RG.ROLE_ID
--                                       FROM GROUP_ROLE RG
--                                                INNER JOIN GROUP_USER GU ON RG.GROUP_ID = GU.GROUP_ID
--                                                INNER JOIN "GROUP" G ON GU.GROUP_ID = G.ID AND G.STATUS = 1
--                                       WHERE GU.USER_ID = P_USER_ID)
--             SELECT O.CODE AS OBJECT_CODE, A.CODE AS ACTION_CODE
--             FROM ROLE_OBJECT_ACTION ROA
--                      INNER JOIN OBJECT O ON ROA.OBJECT_ID = O.ID AND O.APP_ID = V_APP_ID
--                      INNER JOIN ACTION A ON ROA.ACTION_ID = A.ID
--                      INNER JOIN MERGED_ROLE_USER GRU ON ROA.ROLE_ID = GRU.ROLE_ID
--             GROUP BY O.CODE, A.CODE;
--     END IF;
--
-- END;
-- ;
--
-- create PROCEDURE GET_ALLOWED_USERS(P_CLIENT_ID IN VARCHAR2, P_OWNER_TYPE IN VARCHAR2, P_RC OUT SYS_REFCURSOR)
--     IS
-- BEGIN
--     OPEN P_RC FOR
--         WITH ALL_USER AS (SELECT ID, USERNAME, FULLNAME, "TYPE", EMAIL, PHONE_NUMBER, STATUS, LAST_MODIFIED_DATE, CREATED_DATE
--                           FROM "USER"
--                           WHERE CLIENT_ID = P_CLIENT_ID),
--              ALLOWED_USER AS (SELECT ID AS USER_ID
--                               FROM ALL_USER
--                               WHERE "TYPE" = P_OWNER_TYPE
--                               UNION
--                               SELECT DISTINCT RU.USER_ID AS USER_ID
--                               FROM ALL_USER
--                                        INNER JOIN ROLE_USER RU ON RU.USER_ID = ALL_USER.ID
--                                        INNER JOIN ROLE R ON RU.ROLE_ID = R.ID AND R.STATUS = 1
--                                        INNER JOIN ROLE_OBJECT_ACTION ROA ON R.ID = ROA.ROLE_ID
--                                        INNER JOIN CURRENT_OBJECT_ACTION COA
--                                                   ON ROA.OBJECT_ID = COA.OBJECT_ID AND ROA.ACTION_ID = COA.ACTION_ID
--                               WHERE ALL_USER."TYPE" IS NULL
--                                  OR ALL_USER."TYPE" <> P_OWNER_TYPE
--                               UNION
--                               SELECT DISTINCT GU.USER_ID AS USER_ID
--                               FROM ALL_USER
--                                        INNER JOIN GROUP_USER GU ON GU.USER_ID = ALL_USER.ID
--                                        INNER JOIN "GROUP" G ON GU.GROUP_ID = G.ID AND G.STATUS = 1
--                                        INNER JOIN GROUP_ROLE RG ON G.ID = RG.GROUP_ID
--                                        INNER JOIN ROLE R ON RG.ROLE_ID = R.ID AND R.STATUS = 1
--                                        INNER JOIN ROLE_OBJECT_ACTION ROA ON R.ID = ROA.ROLE_ID
--                                        INNER JOIN CURRENT_OBJECT_ACTION COA
--                                                   ON ROA.OBJECT_ID = COA.OBJECT_ID AND ROA.ACTION_ID = COA.ACTION_ID
--                               WHERE ALL_USER."TYPE" IS NULL
--                                  OR ALL_USER."TYPE" <> P_OWNER_TYPE)
--         SELECT ALL_USER.*
--         FROM ALL_USER
--                  INNER JOIN ALLOWED_USER ON ALL_USER.ID = ALLOWED_USER.USER_ID
--         ORDER BY ALL_USER.LAST_MODIFIED_DATE, ALL_USER.CREATED_DATE
--     ;
--
-- END;
-- ;

create sequence if not exists ADMIN_OWNER.CLIENT_ORDINAL_SEQ
;

