-- src/main/resources/db/migration/V1__init_product_schema.sql

CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    price           DECIMAL(10,2)  NOT NULL,
    category        VARCHAR(100)   NOT NULL,
    brand           VARCHAR(100),
    rating          DOUBLE         NOT NULL DEFAULT 0.0,
    review_count    INT            NOT NULL DEFAULT 0,
    image_url       VARCHAR(512),
    tags            VARCHAR(1024),
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_product_category (category),
    INDEX idx_product_brand (brand),
    INDEX idx_product_rating (rating DESC),
    INDEX idx_product_active (active),
    FULLTEXT INDEX idx_product_search (name, description, tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed data for realistic testing
INSERT INTO products (name, description, price, category, brand, rating, review_count, tags) VALUES
('TechPro Gaming Laptop X1', '15.6" FHD 144Hz, Intel i7-13700H, RTX 4060, 16GB DDR5, 512GB NVMe SSD. Perfect for gaming and content creation.', 1299.99, 'electronics', 'TechPro', 4.7, 1284, 'laptop,gaming,rtx,intel,high-performance'),
('SoundMax Pro NC500', 'Wireless noise-cancelling headphones with 30hr battery, multipoint Bluetooth 5.3, and Hi-Res audio support.', 249.99, 'electronics', 'SoundMax', 4.6, 956, 'headphones,wireless,noise-cancelling,bluetooth'),
('AudioElite ANC 300', 'Premium ANC headphones with adaptive noise cancellation, 25hr battery, and LDAC codec support.', 279.99, 'electronics', 'AudioElite', 4.5, 867, 'headphones,wireless,noise-cancelling,premium'),
('BassWave Ultra NC', 'Affordable noise-cancelling headphones with deep bass, 20hr battery, and comfortable fit.', 199.99, 'electronics', 'BassWave', 4.3, 2103, 'headphones,wireless,noise-cancelling,budget'),
('Nike Air Max 270', 'Lightweight running shoe with Max Air unit for all-day comfort. Breathable mesh upper.', 149.99, 'footwear', 'Nike', 4.4, 3567, 'shoes,running,nike,athletic,comfortable'),
('Adidas Ultraboost 23', 'Energy-returning running shoe with BOOST midsole and Primeknit upper.', 189.99, 'footwear', 'Adidas', 4.5, 2890, 'shoes,running,adidas,athletic,boost'),
('Samsung 65" OLED 4K TV', 'Quantum HDR OLED, Neural Quantum Processor, Dolby Atmos. Smart TV with Tizen OS.', 1799.99, 'electronics', 'Samsung', 4.8, 432, 'tv,oled,4k,samsung,smart-tv'),
('Apple iPad Air M2', '11" Liquid Retina display, M2 chip, 128GB, Wi-Fi 6E. Perfect for work and creativity.', 599.99, 'electronics', 'Apple', 4.7, 1567, 'tablet,apple,ipad,m2,portable'),
('Dyson V15 Detect', 'Cordless vacuum with laser dust detection, piezo sensor, and 60min runtime.', 749.99, 'home', 'Dyson', 4.6, 890, 'vacuum,dyson,cordless,home,cleaning'),
('KitchenAid Stand Mixer', '5-quart tilt-head stand mixer with 10 speeds. Includes flat beater, dough hook, and wire whip.', 379.99, 'home', 'KitchenAid', 4.8, 4521, 'mixer,kitchen,baking,cooking,stand-mixer'),
('Lego Technic Ferrari', '3,778-piece Ferrari Daytona SP3 building set with working V12 engine and opening doors.', 449.99, 'toys', 'Lego', 4.9, 678, 'lego,ferrari,building,toys,collector'),
('Patagonia Down Sweater', 'Lightweight, windproof down jacket with 800-fill-power insulation. Packs into its own pocket.', 279.99, 'clothing', 'Patagonia', 4.6, 1234, 'jacket,down,outerwear,patagonia,warm'),
('Instant Pot Duo Plus', '6-quart multi-cooker with 9-in-1 functionality: pressure cook, slow cook, rice, yogurt, and more.', 89.99, 'home', 'Instant Pot', 4.5, 8765, 'cooker,kitchen,multi-cooker,pressure,coaming'),
('Sony WH-1000XM5', 'Industry-leading noise cancellation with 30hr battery, multipoint connection, and LDAC.', 349.99, 'electronics', 'Sony', 4.7, 2345, 'headphones,sony,wireless,noise-cancelling,premium'),
('Bose QuietComfort Ultra', 'Immersive spatial audio with world-class noise cancellation. 24hr battery life.', 429.99, 'electronics', 'Bose', 4.6, 1567, 'headphones,bose,wireless,noise-cancelling,spatial-audio');