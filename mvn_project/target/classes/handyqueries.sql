-- orderproducts table
 
 order_product_id | order_id | product_id
------------------+----------+------------
                0 |        1 |          1
                1 |        1 |          7
                2 |        1 |          3
                3 |        1 |          6
                4 |        1 |         16

-- orders table
 order_id | order_year | order_week | order_day | price |   order_time
----------+------------+------------+-----------+-------+-----------------
    92374 | 2024       | 9          | 3         |  6.25 |
    92375 | 2024       | 9          | 3         |  6.25 |
        1 | 2023       | 01         | 01        | 30.49 | 18:17:09.743738
        2 | 2023       | 01         | 01        | 34.44 | 16:41:17.010902
        3 | 2023       | 01         | 01        | 30.45 | 12:58:16.424713
        4 | 2023       | 01         | 01        | 58.18 | 10:19:45.757064
        5 | 2023       | 01         | 01        | 21.01 | 15:11:33.861389




SELECT *
FROM orders
ORDER BY order_id DESC
LIMIT 20;


SELECT *
FROM order_products
ORDER BY order_id DESC
LIMIT 5;

SELECT *
FROM products
ORDER BY order_id DESC
LIMIT 5;


WITH filtered_orders AS (
    SELECT *
    FROM orders
    WHERE order_year = '2024'
    AND order_week = '52'
    AND order_day = '07'
    AND order_time >= '10:58:00.0'
    AND order_time <= '11:00:00.0'
)
SELECT op.order_id, op.product_id, p.productname
FROM filtered_orders fo
JOIN order_products op ON fo.order_id = op.order_id
JOIN products p ON op.product_id = p.product_id;





all_orders_and_products AS (
    SELECT op.order_id, op.product_id, p.productname
    FROM filtered_orders fo
    JOIN order_products op ON fo.order_id = op.order_id
    JOIN products p ON op.product_id = p.product_id
),    -- this gets all of the products associated with all of the orders 
product_inventory_count AS (
    SELECT pi.inventory_id, COUNT(*) AS usage_count
    FROM order_products op
    JOIN product_inventory pi ON op.product_id = pi.product_id
    GROUP BY pi.inventory_id
)   
SELECT pi.name AS inventory_name, pic.usage_count
FROM product_inventory_count pic
JOIN inventory pi ON pic.inventory_id = pi.id;

SELECT product_inventory.product_id, product_inventory.product_id, inventory.name
FROM product_inventory
JOIN inventory
ON product_inventory.product_id = inventory.id


CREATE TABLE Table1 (
    id INT PRIMARY KEY,
    attribute1 VARCHAR(255)
);

-- Second table
CREATE TABLE Table2 (
    id INT PRIMARY KEY,
    attribute2 INT
);

SELECT inventory_id
WHERE 