<template>

<v-card style="width:300px; margin-top:50px;">
    <template slot="progress">
      <v-progress-linear
        color="deep-purple"
        height="10"
        indeterminate
      ></v-progress-linear>
    </template>

    <v-img
      style="width:290px; height:150px; border-radius:10px; margin-left:5px;"
      src="https://cdn.vuetifyjs.com/images/cards/cooking.png"
    ></v-img>

    <v-card-title>
        OrderList # {{value.id}}
    </v-card-title >

    <v-card-text >

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="Id" v-model="value.id"/>
          </div>
          <div class="grey--text ml-4" v-else>
            Id :  {{value.id }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="OrderId" v-model="value.orderId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            OrderId :  {{value.orderId }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="CustomerId" v-model="value.customerId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            CustomerId :  {{value.customerId }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="ProductId" v-model="value.productId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            ProductId :  {{value.productId }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="DeliveryId" v-model="value.deliveryId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            DeliveryId :  {{value.deliveryId }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field type="number" label="OrderQty" v-model="value.orderQty"/>
          </div>
          <div class="grey--text ml-4" v-else>
            OrderQty :  {{value.orderQty }}
          </div>

          <div class="grey--text ml-4" v-if="editMode">
            <v-text-field label="Status" v-model="value.status"/>
          </div>
          <div class="grey--text ml-4" v-else>
            Status :  {{value.status }}
          </div>



    </v-card-text>

    <v-divider class="mx-4"></v-divider>

    <v-card-actions>
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="edit"
        v-if="!editMode"
      >
        Edit
      </v-btn>
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="save"
        v-else
      >
        Save
      </v-btn>
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="remove"
        v-if="!editMode"
      >
        Delete
      </v-btn>
    </v-card-actions>
  </v-card>


</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'OrderList',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "orderId", value: "orderId" },
            { text: "customerId", value: "customerId" },
            { text: "productId", value: "productId" },
            { text: "deliveryId", value: "deliveryId" },
            { text: "orderQty", value: "orderQty" },
            { text: "status", value: "status" },
        ],
        orderList : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/orderlists')

      this.orderList = temp.data._embedded.orderlists;

    },
    methods: {
    }
  }
</script>

