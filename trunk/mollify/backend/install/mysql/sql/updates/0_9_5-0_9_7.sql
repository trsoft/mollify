ALTER TABLE `user` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `folder` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `item_description` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `item_permission` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `user_folder` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `parameter` CONVERT TO CHARACTER SET utf8;

ALTER TABLE `item_permission` ADD `user_id` int( 11 ) NOT NULL DEFAULT '0' FIRST;
ALTER TABLE `item_permission` DROP PRIMARY KEY, ADD PRIMARY KEY ( `user_id`, `item_id` );

UPDATE 'parameter' SET 'value' = '0_9_7' where 'name' = 'version';