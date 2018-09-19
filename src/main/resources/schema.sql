CREATE TABLE `BLOCKED_IPS` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `ip` varchar(15) DEFAULT NULL,
  `reason` varchar(250) DEFAULT NULL,
  `start_date_param` datetime NOT NULL,
  `duration` varchar(10) NOT NULL,
  `threshold` int(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8