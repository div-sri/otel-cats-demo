create table products.product_info
(
    id          varchar(36)  not null,
    name        varchar(100) not null,
    description varchar(256),
    created_at  timestamp    not null,
    is_active   boolean      not null,
    constraint uk__product_info__name unique (name),
    constraint pk__product_info primary key (id)
);

create table products.product_quantity
(
    product_id varchar(36) not null,
    quantity   double      not null,
    unit       varchar(20) not null,
    constraint pk__product_quantity primary key (product_id),
    constraint fk__product_quantity__product_info
        foreign key (product_id) references products.product_info (id) on delete cascade
);

create table products.product_price
(
    product_id varchar(36) not null,
    price      double      not null,
    currency   varchar(36) not null,
    constraint pk__product_price primary key (product_id),
    constraint fk__product_price__product_info
        foreign key (product_id) references products.product_info (id) on delete cascade
);
