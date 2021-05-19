

<template>
<div>
  <h1>OrderList</h1>

    <div>
        <OrderList v-for="(value, index) in values" v-model="values[index]" v-bind:key="index"/>
    </div>

    Add New OrderList
    <OrderList :isNew="true" :editMode="true" v-model="newValue" @add="append"/>
</div>
</template>

<script>

const axios = require('axios').default;
import OrderList from './OrderList.vue';

export default {
  name: 'OrderListManager',

  components: {
    OrderList
  },

  data: () => ({
    values: [],
    newValue: {}
  }),

  async created() {
      var temp = await axios.get(axios.backend + '/orderlist')

      this.values = temp.data._embedded.orderlist;

  },

  methods:{
    append(value){
      this.values.push(value)
      this.newValue = {}
    }
  }
};
</script>
