INSERT INTO orders (id, description, status, user_id, created_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Order 1 for user1', 'CREATED', 'a34d9da5-c9f8-4ce1-8535-605c8de64219',
        NOW()),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Order 2 for user1', 'IN_PROGRESS',
        'a34d9da5-c9f8-4ce1-8535-605c8de64219', NOW()),
       ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Order for admin', 'CREATED', '3e0b972b-a09f-4a09-9496-e675c1240ae2',
        NOW());