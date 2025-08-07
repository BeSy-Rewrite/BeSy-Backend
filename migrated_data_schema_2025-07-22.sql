INSERT INTO migrated_data.address (
    postal_code,
    building_name,
    building_number,
    comment,
    country,
    county,
    street,
    town
)
SELECT DISTINCT
    address_postal_code,
    address_building_name,
    address_building_number,
    address_comment,
    country_name,           -- FK dropped: now plain text
    address_county,
    address_street,
    address_town
FROM besy.address;

INSERT INTO migrated_data.address (
    building_name,
    street,
    building_number,
    town,
    postal_code,
    county,
    country
)
SELECT DISTINCT
    s.supplier_building_name,
    s.supplier_street,
    s.supplier_building_number,
    s.supplier_town,
    s.supplier_postal_code,
    s.supplier_county,
    c.country_name
FROM besy.supplier s
LEFT JOIN besy.country c ON c.country_name = s.country_name
ON CONFLICT(building_name, street, building_number, town, postal_code, county, country) DO NOTHING;

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
    fax,
    phone,
    title,
    address_id,
    comment,
    email,
    name,
    person_gender,
    surname
)
SELECT
    p.person_fax,
    p.person_phone,
    p.person_title,
    a2.id AS address_id, -- join old `address_name` to new `address.name`
    p.person_comment,
    p.person_email,
    p.person_given_name,
    p.person_gender,
    p.person_surname
FROM besy.person p
JOIN besy.address a1 ON a1.address_name = p.address_name
JOIN migrated_data.address a2
    ON a2.building_name = a1.address_building_name
    AND a2.street = a1.address_street
    AND a2.building_number = a1.address_building_number
    AND a2.town = a1.address_town
    AND a2.postal_code = a1.address_postal_code
    AND a2.county = a1.address_county;


INSERT INTO migrated_data.vat (
    value,
    description
)
SELECT
    vat_value,
    vat_description
FROM besy.vat;

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
         LEFT JOIN migrated_data.address a
                   ON s.supplier_street = a.street
                       AND s.supplier_town = a.town
                       AND s.supplier_postal_code = a.postal_code
                       AND s.supplier_building_name = a.building_name
                        AND s.supplier_building_number = a.building_number
                        AND s.supplier_county = a.county;

INSERT INTO migrated_data."user" (
    keycloak_uuid,
    email,
    name,
    surname
)
SELECT
    u.user_name,
    p.person_email,
    p.person_given_name,
    p.person_surname
FROM besy."user" u
         JOIN besy.person p ON u.person_id = p.person_id;

INSERT INTO migrated_data."order" (
    auto_index,
    booking_year,
    cashback_days,
    cashback_percentage,
    fixed_discount,
    flag_decision_cheapest_offer,
    flag_decision_contract_partner,
    flag_decision_other_reasons,
    flag_decision_sole_supplier,
    flag_edv_permission,
    flag_furniture_permission,
    flag_furniture_room,
    flag_investment_room,
    flag_investment_structural_measures,
    flag_media_permission,
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
    decision_other_reasons_description
)
SELECT
    o.order_auto_index,
    o.order_booking_year,
    o.order_cashback_days,
    o.order_cashback_percentage,
    o.order_fixed_discount,
    o.order_flag_decision_cheapest_offer,
    o.order_flag_decision_contract_partner,
    o.order_flag_decision_other_reasons,
    o.order_flag_decision_sole_supplier,
    o.order_flag_edv_permission,
    o.order_flag_furniture_permission,
    o.order_flag_furniture_room,
    o.order_flag_investment_room,
    o.order_flag_investment_structural_measures,
    o.order_flag_media_permission,
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
    o.owner_user_name,
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
    o.order_decision_other_reasons_description

FROM besy."order" o
         LEFT JOIN besy.person dp_old ON o.delivery_person_id = dp_old.person_id
         LEFT JOIN migrated_data.person dp ON dp_old.person_given_name = dp.name AND dp_old.person_surname = dp.surname

         LEFT JOIN besy.person ip_old ON o.invoice_person_id = ip_old.person_id
         LEFT JOIN migrated_data.person ip ON ip_old.person_given_name = ip.name AND ip_old.person_surname = ip.surname

         LEFT JOIN besy.person qp_old ON o.queries_person_id = qp_old.person_id
         LEFT JOIN migrated_data.person qp ON qp_old.person_given_name = qp.name AND qp_old.person_surname = qp.surname

         LEFT JOIN migrated_data.supplier sp ON sp.name = o.supplier_name;

INSERT INTO migrated_data.item (
    price_per_unit,
    order_id,
    quantity,
    quantity_unit,
    article_id,
    comment,
    item_vat_type,
    preferred_list,
    preferred_list_number,
    vat_value,
    name
)
SELECT
    item_price_per_unit,
    order_id,
    item_quantity,
    item_quantity_unit,
    item_article_id,
    item_comment,
    item_vat_type::text,
    preferred_list_abbr, -- must match new ENUM: 'RZ' or 'TA'
    item_preferred_list_number,
    vat_value,
    item_name
FROM besy.item;
