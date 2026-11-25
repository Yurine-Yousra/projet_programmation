users: data/users.csv
orders: data/orders.csv

R1 = SELECT users age < 25
R = PROJ R1 name
