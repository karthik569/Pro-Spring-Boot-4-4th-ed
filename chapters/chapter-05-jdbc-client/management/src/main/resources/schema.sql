CREATE TABLE IF NOT EXISTS company
(
    company_id   UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    industry     VARCHAR(255),
    website      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS customer
(
    customer_id UUID PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    job_title   VARCHAR(255),
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(50),
    company_id  UUID,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES company (company_id)
);

CREATE TABLE IF NOT EXISTS address
(
    address_id  UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(255) NOT NULL,
    state       VARCHAR(255) NOT NULL,
    zip         VARCHAR(20) NOT NULL,
    CONSTRAINT fk_customer_address FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);

CREATE TABLE IF NOT EXISTS communication
(
    communication_id    UUID PRIMARY KEY,
    customer_id         UUID NOT NULL,
    communication_type  VARCHAR(50) NOT NULL,
    communication_value VARCHAR(255) NOT NULL,
    CONSTRAINT fk_customer_comm FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);