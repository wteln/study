<template>
  <div class="root">
    <el-container v-if="!user.login">
      <el-header height="200px"> </el-header>
      <el-container>
        <el-main>
          <el-form label-width="120px" size="medium">
            <el-row>
              <el-col :span="5" :offset="6">
                <el-form-item label="用户名" required>
                  <el-input v-model="user.name" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="5" :offset="6">
                <el-form-item label="密码" required>
                  <el-input v-model="user.password" show-password />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="5" :offset="8">{{ loginNotice }}</el-col>
            </el-row>
            <el-row>
              <el-col :span="3" :offset="8">
                <el-button type="primary" @click="login">登录</el-button>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
    </el-container>
    <el-container v-if="user.login">
      <el-header>
        <el-menu
          class="menu"
          :default-active="activeIndex"
          mode="horizontal"
          @select="handleSelect"
          background-color="#d4d4d5"
          active-text-color="#a4354a !important"
        >
          <el-menu-item
            style="font-size: 1.5em"
            index="index + ''"
            :key="index"
            v-for="(item, index) in menuItems"
            >{{ item.title }}</el-menu-item
          >
          <el-sub-menu index="2" id="submenu">
            <template style="font-size: 1.5em" #title>统计</template>
            <el-menu-item style="font-size: 1.5em" index="2-1">评分</el-menu-item>
            <el-menu-item style="font-size: 1.5em" index="2-2">类别</el-menu-item>
            <el-menu-item style="font-size: 1.5em" index="2-3">标签</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-header>
      <el-main>
        <div v-if="activeIndex == 0">
          <el-form :inline="true">
            <el-form-item label="电影名">
              <el-input v-model="query.params.name"></el-input>
            </el-form-item>
            <el-form-item label="类型">
              <el-input v-model="query.params.category"></el-input>
            </el-form-item>
            <el-form-item label="标签">
              <el-input v-model="query.params.tag"></el-input>
            </el-form-item>
            <el-form-item label="评分">
              <el-col
                ><el-input v-model="query.params.minRate"></el-input
              ></el-col>
              <el-col :span="10">&nbsp;-&nbsp; </el-col>
              <el-col
                ><el-input v-model="query.params.maxRate"></el-input
              ></el-col>
            </el-form-item>
            <el-form-item label="">
              <el-button
                type="primary"
                @click="beginQuery"
                :disabled="query.begin && !query.complete"
                >查询</el-button
              >
            </el-form-item>
          </el-form>
          <div
            v-if="query.begin || query.complete"
            v-loading="query.begin && !query.complete"
            element-loading-text="Loading..."
          >
            <el-table :data="movies.items" style="width: 100%">
              <el-table-column prop="title" label="电影名" width="600" />
              <el-table-column prop="genres" label="类型" />
              <el-table-column prop="tag" label="标签" />
              <el-table-column prop="rate" label="评分" />
            </el-table>
            <el-pagination
              v-model:currentPage="movies.pageNo"
              :page-size="movies.pageSize"
              :page-sizes="[10, 20, 50, 100, 200]"
              layout="total, sizes, prev, pager, next"
              :total="movies.pageTotal"
              @size-change="reGetMovies"
              @current-change="getMovies"
            ></el-pagination>
          </div>
        </div>
        <div v-if="activeIndex == 1">
          <div>
            <el-button @click="userAdd">添加</el-button>
            <el-button @click="userEdit">修改</el-button>
            <el-button @click="userDelete">删除</el-button>
            <el-table
              ref="user"
              highlight-current-row
              @row-click="handleSelectUser"
              :data="users.items"
              style="width: 100%"
            >
              <el-table-column prop="username" label="用户名" width="600" />
              <el-table-column prop="gender" label="性别" />
              <el-table-column prop="ageDesc" label="年龄" />
              <el-table-column prop="occDesc" label="职业" />
              <el-table-column prop="zipCode" label="邮编" />
            </el-table>
            <el-pagination
              v-model:currentPage="users.pageNo"
              :page-size="users.pageSize"
              layout="total, prev, pager, next"
              :total="users.pageTotal"
              @current-change="listUsers"
            ></el-pagination>
          </div>
        </div>
        <div v-if="activeIndex.startsWith('2-')">
          <div id="chart" :style="{height: chartHeight + 'px', width: '100%'}">
          </div>
        </div>
        <el-dialog
          v-model="userDialogShow"
          :title="dialogType"
          width="30%"
          :before-close="dialogClose"
        >
          <el-form label-width="120px">
            <el-form-item label="用户名">
              <el-input v-model="editUser.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="editUser.password" show-password />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="editUser.gender" placeholder="Select">
                <el-option
                  v-for="item in options.gender"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="年龄">
              <el-select v-model="editUser.age" placeholder="Select">
                <el-option
                  v-for="item in options.age"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="职业">
              <el-select v-model="editUser.occupation" placeholder="Select">
                <el-option
                  v-for="item in options.occupation"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="邮编">
              <el-input v-model="editUser.zipCode" />
            </el-form-item>
            <el-col :span="4" :offset="3">{{ editUserNotic }} </el-col>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="userDialogShow = false">取消</el-button>
              <el-button type="primary" @click="userAct">确定</el-button>
            </span>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import axios from "axios";
import { ElNotification } from "element-plus";
import * as echarts from 'echarts';

axios.defaults.withCredentials = true;
const baseUrl = "http://localhost:5000/movie-manager/";

export default {
  name: "App",
  data() {
    return {
      activeIndex: "0",
      menuItems: [{ title: "电影" }, { title: "用户" }],
      query: {
        begin: false,
        complete: false,
        params: {
          name: "",
          minRate: 0.0,
          maxRate: 5.0,
          category: "",
          tag: "",
        },
        id: "",
      },
      movies: {
        items: [],
        pageNo: 1,
        pageSize: 20,
        pageTotal: 0,
      },
      timer: null,
      user: {
        login: true,
        name: null,
        password: null,
      },
      loginNotice: "",
      users: {
        items: [],
        pageNo: 1,
        pageSize: 50,
        pageTotal: 0,
      },
      userDialogShow: false,
      editUser: {
        username: "",
        password: "",
        gender: "",
        age: "",
        occupation: "",
        zipCode: "",
      },
      options: {
        gender: [
          {
            label: "male",
            value: "male",
          },
          {
            label: "female",
            value: "female",
          },
        ],
        age: [
          {
            label: "18岁以下",
            value: "1",
          },
          {
            label: "18-24",
            value: "18",
          },
          {
            label: "25-34",
            value: "25",
          },
          {
            label: "35-44",
            value: "35",
          },
          {
            label: "45-49",
            value: "45",
          },
          {
            label: "50-55",
            value: "50",
          },
          {
            label: "56岁以上",
            value: "56",
          },
        ],
        occupation: [
          { label: "other or not specified", value: "0" },
          { label: "academic/educator", value: "1" },
          { label: "artist", value: "2" },
          { label: "clerical/admin", value: "3" },
          { label: "college/grad student", value: "4" },
          { label: "customer service", value: "5" },
          { label: "doctor/health care", value: "6" },
          { label: "executive/managerial", value: "7" },
          { label: "farmer", value: "8" },
          { label: "homemaker", value: "9" },
          { label: "K-12 student", value: "10" },
          { label: "lawyer", value: "11" },
          { label: "programmer", value: "12" },
          { label: "retired", value: "13" },
          { label: "sales/marketing", value: "14" },
          { label: "scientist", value: "15" },
          { label: "self-employed", value: "16" },
          { label: "technician/engineer", value: "17" },
          { label: "tradesman/craftsman", value: "18" },
          { label: "unemployed", value: "19" },
          { label: "writer", value: "20" },
        ],
      },
      selectedUser: null,
      editUserNotic: "",
      chartHeight: 500
    };
  },
  created: function () {
    console.log(document);
  },
  methods: {
    handleSelect(index) {
      console.log(index)
      this.activeIndex = index
      if (this.activeIndex == "1") {
        this.listUsers()
      } else if (this.activeIndex.startsWith("2-")) {
        this.showCharts()
      }
    },
    beginQuery() {
      const params = this.query.params;
      this.query.begin = true;
      this.query.complete = false;
      const url = baseUrl + "/queries";
      axios.post(url, params).then((res) => {
        if (this.showErrors(res)) {
          this.query.begin = false;
        } else {
          this.query.id = res.data["obj"];
          this.timer = window.setInterval(this.checkQuery, 1000 * 10);
        }
      });
    },
    checkQuery() {
      const url = baseUrl + "/queries/" + this.query.id + "/status";
      axios.get(url).then((res) => {
        if (this.showErrors(res)) {
          clearInterval(this.timer);
          this.onQueryComplete(false);
        } else {
          if (res.data["obj"] == null) {
            return 0;
          } else {
            clearInterval(this.timer);
            this.onQueryComplete(true);
          }
        }
      });
    },
    onQueryComplete(isSuccess) {
      if (isSuccess) {
        this.query.complete = true;
        this.query.begin = false;
        this.getMovies();
      } else {
        this.query.begin = false;
        this.query.complete = false;
      }
    },
    getMovies() {
      const url = baseUrl + "/queries/" + this.query.id + "/result";
      axios
        .get(url, {
          params: {
            pageNo: this.movies.pageNo,
            pageSize: this.movies.pageSize,
          },
        })
        .then((res) => {
          if (!this.showErrors(res)) {
            const page = res.data["obj"];
            this.movies.items = page["items"];
            this.movies.pageNo = page["pageNo"];
            this.movies.pageSize = page["pageSize"];
            this.movies.pageTotal = page["total"];
            console.log(page);
          }
        });
    },
    reGetMovies() {
      this.movies.pageNo = 1;
      this.getMovies();
    },

    // 用户相关
    login() {
      const url = baseUrl + "/users/login";
      const user = {
        username: this.user.name,
        password: this.user.password,
      };
      if (!this.checkLoginInput()) {
        return;
      }
      axios.post(url, user).then((res) => {
        if (!this.showErrors(res)) {
          this.user.name = res.data["obj"]["username"];
          this.user.password = res.data["obj"]["password"];
          this.user.isAdmin = res.data["obj"]["admin"];
          this.user.login = true;
          this.user.id = res.data["obj"]["id"];
        }
      });
      console.log(document.cookie);
    },
    register() {
      const url = baseUrl + "/users";
      const user = {
        username: this.user.name,
        password: this.user.password,
      };
      if (!this.checkLoginInput()) {
        return;
      }
      axios.post(url, user).then((res) => {
        if (!this.showErrors(res)) {
          this.user.name = res.data["obj"]["username"];
          this.user.password = res.data["obj"]["password"];
          this.user.isAdmin = res.data["obj"]["admin"];
          this.user.login = true;
        }
      });
    },
    checkLoginInput() {
      if (this.user.password.length < 8) {
        this.loginNotice = "密码至少8位";
        return false;
      }
      if (this.user.name.length == 0 || this.user.name.length > 32) {
        this.loginNotice = "用户名过长";
        return false;
      }
      return true;
    },
    listUsers() {
      const url = baseUrl + "/users";
      axios
        .get(url, {
          params: {
            pageNo: this.users.pageNo,
            pageSize: this.users.pageSize,
            userId: this.user.id,
          },
        })
        .then((res) => {
          if (!this.showErrors(res)) {
            const page = res.data["obj"];
            this.users.items = page["items"];
            this.users.pageNo = page["pageNo"];
            this.users.pageSize = page["pageSize"];
            this.users.pageTotal = page["total"];
            console.log(page);
          }
        });
    },
    handleSelectUser(row) {
      console.log(row);
      this.selectedUser = row;
    },
    userAdd() {
      console.log("add user");
      this.editUser.username = "";
      this.editUser.password = "";
      this.editUser.gender = "male";
      this.editUser.age = "1";
      this.editUser.occupation = "0";
      this.editUser.zipCode = "";
      this.userDialogShow = true;
      this.dialogType = "添加用户";
      this.userAction = "add";
    },
    userEdit() {
      if (this.selectedUser == null) {
        this.showError("请先选中一个用户");
      }
      if (
        this.selectedUser.username == null ||
        this.selectedUser.username == ""
      ) {
        this.showError("只允许修改admin用户的信息");
        return;
      }
      this.editUser.username = this.selectedUser.username;
      this.editUser.password = "";
      this.editUser.gender = this.selectedUser.gender;
      this.editUser.age = this.selectedUser.age;
      this.editUser.occupation = this.selectedUser.occupation;
      this.editUser.zipCode = this.selectedUser.zipCode;
      this.userDialogShow = true;
      this.userAction = "edit";
      this.dialogType = "修改用户信息";
    },
    userDelete() {
      if (this.selectedUser == null) {
        this.showError("请先选中一个用户");
      }
      const url = baseUrl + "/users/" + this.selectedUser.id;
      axios.delete(url).then((res) => {
        if (!this.showErrors(res)) {
          this.selectedUser = null;
          this.listUsers();
        }
      });
    },
    userAct() {
      if (!this.checkLoginInput()) {
        return;
      }
      console.log("action type " + this.userAction)
      if (this.userAction == "add") {
        const url = baseUrl + "/users";
        const param = {
          username: this.editUser.username,
          password: this.editUser.password,
          gender: this.editUser.gender,
          age: this.editUser.age,
          occupation: this.editUser.occupation,
          zipCode: this.editUser.zipCode,
        };
        axios.post(url, param).then((res) => {
          if (!this.showErrors(res)) {
            this.userDialogShow = false;
            this.listUsers();
          }
        });
      } else if (this.userAction == "edit") {
        const url = baseUrl + "/users";
        const param = {
          id: this.selectedUser.id,
          username: this.editUser.username,
          password: this.editUser.password,
          gender: this.editUser.gender,
          age: this.editUser.age,
          occupation: this.editUser.occupation,
          zipCode: this.editUser.zipCode,
        };
        axios.put(url, param).then((res) => {
          if (!this.showErrors(res)) {
            this.userDialogShow = false;
            this.listUsers();
          }
        });
      }
    },
    checkActInput() {
      if (this.editUser.password.length < 8) {
        this.editUserNotic = "密码至少8位";
        return false;
      }
      if (
        this.editUser.username.length == 0 ||
        this.editUser.username.length > 32
      ) {
        this.editUserNotic = "用户名过长或过短";
        return false;
      }
      if (
        this.editUser.occupation.length == 0 ||
        this.editUser.occupation.length > 128
      ) {
        this.editUserNotic = "职业过长或过短";
        return false;
      }
      if (
        this.zipCode.occupation.length == 0 ||
        this.zipCode.occupation.length > 32
      ) {
        this.editUserNotic = "邮编不合法";
        return false;
      }
      return true;
    },
    dialogClose() {
      this.userDialogShow = false;
    },
    showCharts() {
      var chartType = "bar"
      var type = "tag"
      var vertical = true
      var log = false
      if(this.activeIndex == "2-1") {
        chartType = "line"
        type = "rate"
        vertical = false
      } else if(this.activeIndex == "2-2") {
        chartType = "bar"
        type = "genre"
        log = true
      } 
      const url = baseUrl + "/charts?type=" + type
      const func = this.drawChart
      axios.get(url).then(res => {
        if(!this.showErrors(res)) {
          const numLabels = res.data['obj']['xs'].length
          this.chartHeight = numLabels * 20 + numLabels * 20
          console.log(numLabels, this.chartHeight)
          if(this.chartHeight > 10000) {
            this.chartHeight = 10000
          }
          window.setTimeout(()=>{
            func(chartType, vertical, log, res.data['obj']['xs'], res.data['obj']['ys'])
          }, 1000)
        }
      })

    },
    drawChart(type, vertical, log, xs, ys) {
      const chartDom = document.getElementById('chart');
      const myChart = echarts.init(chartDom);      
      
      const option = {}
      const xAxis= {
        type: 'category',
        data: xs
      }
      const yAxis= {
        type: 'value'
      }
      const series= [
        {
          data: ys,
          type: type
        }
      ]

      if(log) {
        yAxis.type = "log"
      }

      option.xAxis = vertical ? yAxis : xAxis
      option.yAxis = vertical ? xAxis : yAxis

      option.series = series

      console.log(option)
      myChart.setOption(option)
    },
    showErrors(res) {
      if (res.status != 200) {
        this.showError(res.statusText);
        return true;
      } else {
        if (res.data["status"] != 0) {
          this.showError(res.data["msg"]);
          return true;
        }
      }
      return false;
    },
    showError(text) {
      ElNotification({
        title: "错误",
        message: text,
        type: "error",
      });
    },
  },
};
</script>

<style>
.menu {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.menu-item {
  font-size: 20em;
}

#submenu>div {
  font-size: 1.5em;
}
</style>
