INSERT INTO migrated_data.address (
    postal_code,
    building_name,
    building_number,
    comment,
    country,
    county,
    street,
    town,
    legacy_address_name,
    owner_type
)
SELECT DISTINCT
    a.address_postal_code,
    a.address_building_name,
    a.address_building_number,
    a.address_comment,
    c.country_german,           -- FK dropped: now plain text
    a.address_county,
    a.address_street,
    a.address_town,
    a.address_name,
    'Person'
FROM besy.address a
JOIN besy.country c ON c.country_name = a.country_name;


-- Map addresses of suppliers to address table.
INSERT INTO migrated_data.address (
    building_name,
    street,
    building_number,
    town,
    postal_code,
    county,
    country,
    legacy_supplier_name,
    owner_type
)
SELECT
    s.supplier_building_name,
    s.supplier_street,
    s.supplier_building_number,
    s.supplier_town,
    s.supplier_postal_code,
    s.supplier_county,
    c.country_german,
    s.supplier_name,
    'Supplier'
FROM besy.supplier s
LEFT JOIN besy.country c ON c.country_name = s.country_name;


INSERT INTO migrated_data.cost_center (
    id,
    name,
    begin_date,
    end_date,
    comment
)
SELECT
    cost_center_id,
    cost_center_name,
    cost_center_begin_date,
    cost_center_end_date,
    cost_center_comment
FROM besy.cost_center;


INSERT INTO migrated_data.currency (
    code,
    name
)
SELECT
    currency_short,
    currency_long
FROM besy.currency;


INSERT INTO migrated_data.person (
    id,
    fax,
    phone,
    title,
    address_id,
    comment,
    email,
    name,
    gender,
    surname
)
SELECT
    p.person_id,
    p.person_fax,
    p.person_phone,
    p.person_title,
    a.id AS address_id, -- join old `address_name` to new `address.name`
    p.person_comment,
    p.person_email,
    p.person_given_name,
    p.person_gender,
    p.person_surname
FROM besy.person p
JOIN migrated_data.address a ON a.legacy_address_name = p.address_name;


INSERT INTO migrated_data.vat (
    value,
    description
)
SELECT
    vat_value,
    vat_description
FROM besy.vat;


INSERT INTO migrated_data.invoice (
    id,
    price,
    date,
    created_date,
    order_id,
    cost_center_id,
    comment
)
SELECT
    invoice_id,
    invoice_price,
    invoice_date,
    invoice_created_date,
    order_id,
    cost_center_id,
    invoice_comment
FROM besy.invoice;


INSERT INTO migrated_data.supplier (
    deactivated_date,
    flag_preferred,
    vat_id,
    email,
    fax,
    phone,
    comment,
    website,
    address_id,
    name
)
SELECT
    supplier_deactivated_date,
    supplier_flag_preferred,
    supplier_vat_id,
    NULL, -- `supplier_email` did not exist
    supplier_fax,
    supplier_phone,
    supplier_comment,
    supplier_website,
    a.id,
    supplier_name
FROM besy.supplier s
         JOIN migrated_data.address a ON a.legacy_supplier_name = s.supplier_name;


INSERT INTO migrated_data."user" (
    email,
    name,
    surname,
    legacy_user_name
)
SELECT
    p.person_email,
    p.person_given_name,
    p.person_surname,
    u.user_name
FROM besy."user" u
         JOIN besy.person p ON u.person_id = p.person_id;


-- This needs to go after suppliers
INSERT INTO migrated_data.customer_id (
    comment,
    customer_id,
    supplier_id
)
SELECT
    b.customer_id_comment,
    b.customer_id,
    s.id
FROM besy.customer_id b
         JOIN migrated_data.supplier s
              ON s.name = b.supplier_name;


INSERT INTO migrated_data."order" (
    id,
    auto_index,
    booking_year,
    cashback_days,
    cashback_percentage,
    fixed_discount,
    flag_decision_cheapest_offer,
    flag_decision_contract_partner,
    flag_decision_other_reasons,
    flag_decision_sole_supplier,
    legacy_alias,
    percentage_discount,
    quote_date,
    quote_price,
    created_date,
    delivery_person_id,
    invoice_person_id,
    queries_person_id,
    last_updated_time,
    dfg_key,
    comment,
    content_description,
    currency_short,
    customer_id,
    owner_user_id,
    primary_cost_center_id,
    quote_number,
    quote_sign,
    secondary_cost_center_id,
    status,
    supplier_id,
    comment_for_supplier,
    decision_other_reasons_description,
    delivery_address_id,
    invoice_address_id,
    flag_decision_most_economical_offer,
    flag_decision_preferred_supplier_list
)
SELECT
    o.order_id,
    o.order_auto_index,
    o.order_booking_year,
    o.order_cashback_days,
    o.order_cashback_percentage,
    o.order_fixed_discount,
    o.order_flag_decision_cheapest_offer,
    o.order_flag_decision_contract_partner,
    o.order_flag_decision_other_reasons,
    o.order_flag_decision_sole_supplier,
    o.order_legacy_alias,
    o.order_percentage_discount,
    o.order_quote_date,
    o.order_quote_price,
    o.order_created_date,

    dp.id AS delivery_person_id,
    ip.id AS invoice_person_id,
    qp.id AS queries_person_id,

    o.order_last_updated_time,
    o.order_dfg_key,
    o.order_comment,
    o.order_content_description,
    o.currency_short,
    o.customer_id,
    u.id,
    o.primary_cost_center_id,
    o.order_quote_number,
    o.order_quote_sign,
    o.secondary_cost_center_id,
    CASE o.order_status
        WHEN 'Abgerechnet' THEN 'ABR'
        WHEN 'Abgeschickt' THEN 'ABS'
        WHEN 'Archiviert' THEN 'ARC'
        WHEN 'Gelöscht' THEN 'DEL'
        WHEN 'In Bearbeitung' THEN 'INB'
        ELSE NULL
        END AS status,

    sp.id,

    o.order_comment_for_supplier,
    o.order_decision_other_reasons_description,

    del_add_new.id,
    inv_add_new.id,

    false,
    false


FROM besy."order" o
        JOIN migrated_data.person dp ON dp.id = o.delivery_person_id

        JOIN migrated_data.person ip ON ip.id = o.invoice_person_id

        JOIN migrated_data.person qp ON qp.id = o.queries_person_id

        JOIN migrated_data.supplier sp ON sp.name = o.supplier_name

        JOIN migrated_data."user" u ON u.legacy_user_name = o.owner_user_name

        -- Insert delivery address by retrieving the old delivery_person's address and mapping it to the new address ID
        JOIN besy.person del_add_p_old ON del_add_p_old.person_id = o.delivery_person_id
        JOIN besy.address del_add_old ON del_add_p_old.address_name = del_add_old.address_name
        JOIN migrated_data.address del_add_new ON del_add_new.legacy_address_name = del_add_old.address_name

        -- Insert invoice address by retrieving the old invoice_person's address and mapping it to the new address ID
        JOIN besy.person inv_add_p_old ON inv_add_p_old.person_id = o.invoice_person_id
        JOIN besy.address inv_add_old ON inv_add_old.address_name = inv_add_p_old.address_name
        JOIN migrated_data.address inv_add_new ON inv_add_new.legacy_address_name = inv_add_old.address_name;


INSERT INTO migrated_data.approvals (
    order_id,
    flag_edv_permission,
    flag_furniture_permission,
    flag_furniture_room,
    flag_investment_room,
    flag_investment_structural_measures,
    flag_media_permission
)
SELECT
    o.order_id,
    o.order_flag_edv_permission,
    o.order_flag_furniture_permission,
    o.order_flag_furniture_room,
    o.order_flag_investment_room,
    o.order_flag_investment_structural_measures,
    o.order_flag_media_permission
FROM besy."order" o;


INSERT INTO migrated_data.item(
    item_id,
   price_per_unit,
   order_id,
   quantity,
   quantity_unit,
   article_id,
   comment,
   vat_type,
   preferred_list, -- enum
   preferred_list_number,
   vat_value,
   name
)
SELECT
        item_id,
       item_price_per_unit,
       order_id,
       item_quantity,
       item_quantity_unit,
       item_article_id,
       item_comment,
       item_vat_type,
       preferred_list_abbr,
       item_preferred_list_number,
       vat_value,
       item_name

FROM besy.item i;


INSERT INTO migrated_data.quotation(
    index,
    price,
    quote_date,
    order_id,
    company_name,
    company_city
)
SELECT q.quotation_index, q.quotation_price, q.quotation_quote_date, q.order_id, q.quotation_company_name, q.quotation_company_city
FROM besy.quotation q;


SELECT setval('migrated_data.person_id_seq', (SELECT MAX(id) FROM migrated_data.person));
SELECT setval('migrated_data.order_id_seq', (SELECT MAX(id) FROM migrated_data."order"));

