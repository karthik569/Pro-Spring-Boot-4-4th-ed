CREATE TABLE IF NOT EXISTS companies (
    company_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255),
    industry VARCHAR(255),
    website VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    job_title VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    company_id UUID REFERENCES companies(company_id)
);

CREATE TABLE IF NOT EXISTS addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(customer_id),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS communications (
    communication_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(customer_id),
    communication_type VARCHAR(255),
    communication_value VARCHAR(255)
);
