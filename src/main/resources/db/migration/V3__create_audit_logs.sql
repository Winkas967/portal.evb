CREATE TABLE audit_logs (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entity_type VARCHAR(30) NOT NULL,
    entity_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    performed_by_user_id INT,
    performed_by_name VARCHAR(50) NOT NULL,
    performed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    details VARCHAR(255),

    CONSTRAINT fk_audit_logs_user FOREIGN KEY (performed_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs(performed_at);