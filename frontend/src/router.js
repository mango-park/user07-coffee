
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import CustomerManager from "./components/CustomerManager"

import OrderManager from "./components/OrderManager"

import ProductManager from "./components/ProductManager"

import DeliveryManager from "./components/DeliveryManager"


import OrderList from "./components/orderList"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Customer',
                name: 'CustomerManager',
                component: CustomerManager
            },

            {
                path: '/Order',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/Product',
                name: 'ProductManager',
                component: ProductManager
            },

            {
                path: '/Delivery',
                name: 'DeliveryManager',
                component: DeliveryManager
            },


            {
                path: '/orderList',
                name: 'orderList',
                component: orderList
            },


    ]
})
