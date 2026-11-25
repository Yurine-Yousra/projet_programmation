users: data/users.csv
orders: data/orders.csv

R1 = PROJ users id name
R2 = JOIN R1 orders id user_id
R = PROJ R2 name amount
