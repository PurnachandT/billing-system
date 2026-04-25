CREATE TABLE organization (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  billing_start_date DATE,
  next_billing_date DATE,
  status VARCHAR(20),
  payment_blocked BOOLEAN,
  razorpay_customer_id VARCHAR(255)
);

CREATE TABLE seat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT,
  user_email VARCHAR(255),
  type VARCHAR(50),
  active BOOLEAN
);

CREATE TABLE invoice (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT,
  seat_count INT,
  amount INT,
  billing_date DATE,
  seat_type VARCHAR(50),
  prorated BOOLEAN,
  status VARCHAR(20),
  quantity INT,
  price_per_seat INT
);