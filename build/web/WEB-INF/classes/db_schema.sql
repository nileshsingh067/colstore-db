/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  nilesh
 * Created: 30 Mar, 2019
 */


CREATE TABLE `db_colstore`.`tb_dblist` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `db_name` VARCHAR(45) NULL,
  `status` INT NULL,
  `created_on` DATETIME NULL,
  `last_update` DATETIME NULL,
  PRIMARY KEY (`id`));


CREATE TABLE `db_colstore`.`db_tbllist` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `db_id` INT NULL,
  `tbl_name` VARCHAR(45) NULL,
  `status` INT NULL,
  `no_of_col` INT NULL,
  `created_on` DATETIME NULL,
  `last_update` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_db_id_idx` (`db_id` ASC),
  CONSTRAINT `fk_db_id`
    FOREIGN KEY (`db_id`)
    REFERENCES `db_colstore`.`tb_dblist` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);







CREATE TABLE `db_colstore`.`tb_collist` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tbl_id` INT NULL,
  `col_name` VARCHAR(45) NULL,
  `col_dataType` ENUM('INT', 'STRING') NULL,
  `status` INT NULL,
  `created_on` DATETIME NULL,
  `last_updated` DATETIME NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tb_tbl_id`
    FOREIGN KEY (`id`)
    REFERENCES `db_colstore`.`db_tbllist` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


