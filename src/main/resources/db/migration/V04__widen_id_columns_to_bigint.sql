DO $$
DECLARE
    r record;
BEGIN
    -- 1) Alle FKs sichern, die eine der umzutypenden Spalten verwenden
    CREATE TEMP TABLE _fk_backup ON COMMIT DROP AS
    SELECT nsp.nspname AS sch,
           rel.relname AS tbl,
           con.conname AS name,
           pg_get_constraintdef(con.oid) AS def
    FROM pg_constraint con
    JOIN pg_class rel      ON rel.oid = con.conrelid
    JOIN pg_namespace nsp  ON nsp.oid = rel.relnamespace
    JOIN pg_attribute att  ON att.attrelid = con.conrelid
                          AND att.attnum = ANY (con.conkey)
    WHERE con.contype = 'f'
      AND nsp.nspname = 'migrated_data'
      AND att.attname IN (
          'owner_user_id', 'delivery_person_id',
          'invoice_person_id', 'queries_person_id',
          'order_id', 'user_id'
      );

    -- 2) Diese FKs droppen
    FOR r IN SELECT * FROM _fk_backup LOOP
        EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I', r.sch, r.tbl, r.name);
    END LOOP;

    -- 3) Spaltentypen erweitern (int4 -> bigint)
    ALTER TABLE migrated_data."user"  ALTER COLUMN id TYPE bigint;
    ALTER TABLE migrated_data."order" ALTER COLUMN id TYPE bigint;
    ALTER TABLE migrated_data.person  ALTER COLUMN id TYPE bigint;

    ALTER TABLE migrated_data."order" ALTER COLUMN owner_user_id      TYPE bigint;
    ALTER TABLE migrated_data."order" ALTER COLUMN delivery_person_id TYPE bigint;
    ALTER TABLE migrated_data."order" ALTER COLUMN invoice_person_id  TYPE bigint;
    ALTER TABLE migrated_data."order" ALTER COLUMN queries_person_id  TYPE bigint;

    ALTER TABLE migrated_data.item                 ALTER COLUMN order_id TYPE bigint;
    ALTER TABLE migrated_data.quotation            ALTER COLUMN order_id TYPE bigint;
    ALTER TABLE migrated_data.invoice              ALTER COLUMN order_id TYPE bigint;
    ALTER TABLE migrated_data.approvals            ALTER COLUMN order_id TYPE bigint;
    ALTER TABLE migrated_data.order_status_history ALTER COLUMN order_id TYPE bigint;

    -- user_preferences.user_id -> FK auf user.id (muss bigint werden)
    ALTER TABLE migrated_data.user_preferences     ALTER COLUMN user_id  TYPE bigint;

    -- 4) FKs exakt wie vorher wiederherstellen
    FOR r IN SELECT * FROM _fk_backup LOOP
        EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I %s',
                       r.sch, r.tbl, r.name, r.def);
    END LOOP;
END $$;

-- 5) Reine Typ-Spalten ohne FK
ALTER TABLE migrated_data.invoice ALTER COLUMN paperless_id TYPE bigint;
ALTER TABLE migrated_data.item    ALTER COLUMN quantity     TYPE bigint;
