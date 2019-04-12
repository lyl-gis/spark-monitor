create table if not exists `tb_task_model`
(
    `id`              varchar(32),
    `name`            varchar(32),
    `main_class`      varchar(255),
    `jar_file`        varchar(255),
    `state`           varchar(12),
    `final_status`    varchar(12),
    `result`          varchar(255),
    `args`            varchar(255),
    `driver_memory`   varchar(12),
    `executor_memory` varchar(12),
    `num_executors`   integer,
    `executor_cores`  integer,
    `parallelism`     integer,
    primary key (`id`)
);