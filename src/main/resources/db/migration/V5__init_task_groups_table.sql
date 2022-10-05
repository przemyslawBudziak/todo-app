create table task_groups
    (
        id          int primary key auto_increment,
        description varchar(100) not null,
        done        bit
);
alter table TASKS add column task_group_id int null;
alter table TASKS add foreign key (task_group_id) references task_groups (id)