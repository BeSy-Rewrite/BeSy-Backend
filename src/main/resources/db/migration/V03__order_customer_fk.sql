DO $$ BEGIN
    ALTER TABLE "order"
        ADD CONSTRAINT fk_order_customer_id_supplier_id
        FOREIGN KEY (customer_id, supplier_id)
        REFERENCES customer_id (customer_id, supplier_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;
