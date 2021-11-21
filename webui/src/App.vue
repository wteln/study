<template>
  <div class="root">
  <el-container v-if="!user.login">
    <el-header height="200px">
    </el-header>
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
            <el-col :span="2" :offset="8">
              <el-button type="primary" @click="login" >登录</el-button>
              &nbsp;&nbsp;
              <el-button type="primary" @click="register">注册</el-button>
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
        <el-menu-item style="font-size:1.5em" :index="index" :key="index" v-for="(item,index) in menuItems">{{item.title}}</el-menu-item>
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
            <el-col><el-input v-model="query.params.minRate"></el-input></el-col>
            <el-col :span="10">&nbsp;-&nbsp; </el-col>
            <el-col><el-input v-model="query.params.maxRate"></el-input></el-col>
          </el-form-item>
          <el-form-item label="">
            <el-button type="primary" @click="beginQuery" :disable="query.begin && !query.complete">查询</el-button>
          </el-form-item>
        </el-form>
        <div v-if="query.begin || query.complete"
         v-loading="query.begin && !query.complete"
         element-loading-text="Loading...">
          <el-table :data="movies.items" style="width: 100%">
            <el-table-column prop="title" label="电影名" width="600" />
            <el-table-column prop="genres" label="类型" />
            <el-table-column prop="tag" label="标签" />
            <el-table-column prop="rate" label="评分" />
          </el-table>
          <el-pagination
            v-model:currentPage="movies.pageNo"
            :page-size="movies.pageSize"
            :page-sizes="[10, 20, 50,100, 200]"
            layout="total, sizes, prev, pager, next"
            :total="movies.pageTotal"
            @size-change="reGetMovies"
            @current-change="getMovies"
          ></el-pagination>
        </div>
      </div>
      <div v-if="activeIndex == 1">
        <div v-if="user.isAdmin">
          <el-table :data="users.items" style="width: 100%">
            <el-table-column prop="username" label="用户名" width="600" />
            <el-table-column prop="admin" label="是否是管理员" />
          </el-table>
          <el-pagination
            v-model:currentPage="users.pageNo"
            :page-size="users.pageSize"
            layout="total, sizes, prev, pager, next"
            :total="users.pageTotal"
            @current-change="listUsers"
          ></el-pagination>
        </div>
        <div v-else>
          <h4>只有管理员可以查看用户信息</h4>
        </div>
      </div>
    </el-main>

  </el-container>
  </div>
</template>

<script>
import axios from 'axios'
import { ElNotification } from 'element-plus'

axios.defaults.withCredentials = true;
const baseUrl = "http://localhost:5000/movie-manager/"

export default {
  name: 'App',
  data() {
    return {
      activeIndex: 0,
      menuItems : [{title: '电影'}, {title: '用户'}],
      query: {
        begin: false,
        complete: false,
        params: {
          name: "",
          minRate: 0.0,
          maxRate: 5.0,
          category: "",
          tag: ""
        },
        id: ""
      },
      movies : {
        items: [],
        pageNo: 1,
        pageSize: 20,
        pageTotal: 0
      },
      timer: null,
      user: {
        login: false,
        name: null,
        password: null,
        isAdmin: false
      },
      loginNotice: "",
      users: {
        items: [],
        pageNo: 1,
        pageSize: 50,
        pageTotal: 0
      }
    }
  },
  created: function () {
    console.log(document)
  },
  methods: {
    handleSelect(index) {
      console.log(index)
      this.activeIndex = index
      if(this.activeIndex == 1) {
        if(this.user.isAdmin) {
          this.listUsers()
        }
      }
    },
    beginQuery() {
      const params = this.query.params
      this.query.begin = true
      this.query.complete = false
      const url = baseUrl + "/queries"
      axios.post(url, params).then(res => {
        if(this.showErrors(res)) {
          this.query.begin = false
        } else {
          this.query.id = res.data['obj']
          this.timer = window.setInterval(this.checkQuery, 1000 * 10);
        }
      })
    },
    checkQuery() {
      const url = baseUrl + "/queries/" + this.query.id + "/status"
      axios.get(url).then(res => {
        if(this.showErrors(res)) {
          clearInterval(this.timer)
          this.onQueryComplete(false)
        } else {
          if(res.data['obj'] == null) {
            return 0
          } else {
            clearInterval(this.timer)
            this.onQueryComplete(true)
          }
        }
      })
    },
    onQueryComplete(isSuccess) {
      if(isSuccess) {
        this.query.complete = true
        this.query.begin = false
        this.getMovies()
      } else {
        this.query.begin = false
        this.query.complete = false
      }
    },
    getMovies() {
      const url = baseUrl + "/queries/" + this.query.id + "/result"
      axios.get(url, {
        params: {
          pageNo: this.movies.pageNo,
          pageSize: this.movies.pageSize
        }
      }).then((res)=>{
        if(!this.showErrors(res)) {
          const page = res.data['obj']
          this.movies.items = page['items']
          this.movies.pageNo = page['pageNo']
          this.movies.pageSize = page['pageSize']
          this.movies.pageTotal = page['total']
          console.log(page)
        }

      })
    },
    reGetMovies() {
      this.movies.pageNo = 1
      this.getMovies()
    },

    // 用户相关
    login() {
      const url = baseUrl + "/users/login"
      const user = {
        username: this.user.name,
        password: this.user.password
      }
      if(!this.checkLoginInput()) {
        return
      }
      axios.post(url, user).then(res => {
        if(!this.showErrors(res)) {
          this.user.name = res.data['obj']['username']
          this.user.password = res.data['obj']['password']
          this.user.isAdmin = res.data['obj']['admin']
          this.user.login = true
          this.user.id = res.data['obj']['id']
        }
      })
      console.log(document.cookie)
    },
    register() {
      const url = baseUrl + "/users"
      const user = {
        username: this.user.name,
        password: this.user.password
      }
      if(!this.checkLoginInput()) {
        return
      }
      axios.post(url, user).then(res => {
        if(!this.showErrors(res)) {
          this.user.name = res.data['obj']['username']
          this.user.password = res.data['obj']['password']
          this.user.isAdmin = res.data['obj']['admin']
          this.user.login = true
        }
      })
    },
    checkLoginInput() {
      if(this.user.password.length < 8) {
        this.loginNotice = "密码至少8位"
        return false
      }
      if(this.user.name.length == 0 || this.user.name.length > 32) {
        this.loginNotice = "用户名过长"
        return false
      }
      return true
    },
    listUsers() {
      const url = baseUrl + "/users"
      axios.get(url, {
        params: {
          pageNo: this.users.pageNo,
          pageSize: this.users.pageSize,
          userId: this.user.id,
        }
      }).then((res)=>{
        if(!this.showErrors(res)) {
          const page = res.data['obj']
          this.users.items = page['items']
          this.users.pageNo = page['pageNo']
          this.users.pageSize = page['pageSize']
          this.users.pageTotal = page['total']
          console.log(page)
        }
      })
    },
    showErrors(res) {
      if(res.status != 200) {
        ElNotification({
          title: '错误',
          message: res.statusText,
          type: 'error',
        })
        return true
      } else {
        if(res.data['status'] != 0) {
          ElNotification({
            title: '错误',
            message: res.data['msg'],
            type: 'error',
          })
          return true
        }
      }
      return false
    }
  }
}
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
</style>
