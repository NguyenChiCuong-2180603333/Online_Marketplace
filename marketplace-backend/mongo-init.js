db = db.getSiblingDB('marketplace');

// Create user
db.createUser({
  user: 'marketplace_user',
  pwd: 'marketplace123',
  roles: [
    {
      role: 'readWrite',
      db: 'marketplace'
    }
  ]
});

// Create collections with indexes
db.createCollection('users');
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "createdAt": 1 });

db.createCollection('products');
db.products.createIndex({ "name": "text", "description": "text" });
db.products.createIndex({ "category": 1 });
db.products.createIndex({ "price": 1 });
db.products.createIndex({ "sellerId": 1 });
db.products.createIndex({ "createdAt": -1 });

db.createCollection('orders');
db.orders.createIndex({ "userId": 1 });
db.orders.createIndex({ "createdAt": -1 });
db.orders.createIndex({ "status": 1 });
db.orders.createIndex({ "paymentId": 1 });

db.createCollection('reviews');
db.reviews.createIndex({ "productId": 1 });
db.reviews.createIndex({ "userId": 1 });
db.reviews.createIndex({ "createdAt": -1 });

db.createCollection('carts');
db.carts.createIndex({ "userId": 1 }, { unique: true });

db.createCollection('notifications');
db.notifications.createIndex({ "userId": 1 });
db.notifications.createIndex({ "createdAt": -1 });
db.notifications.createIndex({ "isRead": 1 });

print('Database initialized successfully!');