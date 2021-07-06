DROP TABLE IF EXISTS characters;

CREATE TABLE characters (
  id INT AUTO_INCREMENT PRIMARY KEY,
  marvelid INT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL,
  description_overflow VARCHAR(255) NOT NULL,
  thumbnail VARCHAR(250) DEFAULT NULL
);