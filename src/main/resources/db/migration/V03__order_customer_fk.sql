DO $$ BEGIN
    ALTER TABLE migrated_data."order"
        ADD CONSTRAINT fk_order_customer_id_supplier_id
        FOREIGN KEY (customer_id, supplier_id)
        REFERENCES migrated_data.customer (customer_id, supplier_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;
