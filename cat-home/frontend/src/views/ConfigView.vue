<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a :href="applicationUrl">Application</a>
        <a class="is-active" :href="configUrl({ op: 'projects' })">Configs</a>
        <a :href="documentUrl">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <TopbarUser />
      </div>
    </header>

    <div class="cat-body">
      <ConfigSidebar
        :active-item="activeSidebarItem"
        :context-path="contextPath"
        :current-domain="currentDomain"
        :exception-tab="exceptionTab"
      />

      <section class="cat-content">
        <section class="config-card">
          <header v-if="showConfigHeading || showConfigActions" class="config-heading">
            <h1 v-if="showConfigHeading">{{ pageTitle }}</h1>
            <span v-else></span>
            <a v-if="isProjectAdd" class="config-primary" :href="configUrl({ op: 'projects' })">返回</a>
            <a v-else-if="isDomainGroupEdit" class="config-primary" :href="configUrl({ op: 'domainGroupConfigs' })">返回</a>
            <a v-else-if="isBusinessCustomAdd" class="config-primary" :href="configUrl({ op: 'businessList', domain: currentDomain })">返回</a>
            <a v-else-if="isAlertRuleUpdate" class="config-primary" :href="configUrl({ op: alertRuleListAction, domain: currentDomain })">返回</a>
            <a v-else-if="isHeartbeatRuleUpdate" class="config-primary" :href="configUrl({ op: 'heartbeatRuleConfigList', domain: currentDomain })">返回</a>
            <a v-else-if="isExceptionEdit" class="config-primary" :href="configUrl({ op: 'exception', type: exceptionTab })">返回</a>
            <a v-else-if="isDomainGroupList" class="config-primary" :href="configUrl({ op: 'domainGroupConfigUpdate' })">
              <Plus class="button-icon" />
              添加
            </a>
            <a v-else-if="isBusinessList" class="config-primary" :href="configUrl({ op: 'businessCustomAdd', domain: currentDomain })">
              <Plus class="button-icon" />
              新增
            </a>
            <a v-else-if="isAlertRule" class="config-primary" :href="configUrl({ op: alertRuleUpdateAction, domain: currentDomain })">
              <Plus class="button-icon" />
              新增
            </a>
            <a v-else-if="isHeartbeatRuleList" class="config-primary" :href="configUrl({ op: 'heartbeatRuleUpdate', domain: currentDomain })">
              <Plus class="button-icon" />
              新增
            </a>
            <a v-else-if="isProjectConfig" class="config-primary" :href="configUrl({ op: 'projectAdd' })">添加</a>
          </header>

          <form v-if="showProjectSearch" class="config-search" @submit.prevent="searchProject">
            <el-autocomplete
              v-model="domainInput"
              class="config-domain-input"
              placeholder="input domain for search"
              :fetch-suggestions="searchDomains"
              value-key="value"
              clearable
              @select="selectDomain"
            />
            <button class="domain-go" type="submit">Go</button>
            <span class="config-search-help">
              请输入你的项目，默认是cat。找不到你的项目？请点
              <a :href="configUrl({ op: 'projectAdd' })">添加</a>
            </span>
          </form>

          <section v-if="loadError" class="empty-state">{{ loadError }}</section>
          <section v-else-if="loading" class="empty-state">正在加载项目配置...</section>
          <section v-else-if="isDomainGroupList" class="config-project-form">
            <p v-if="report?.opState === true" class="config-state">操作成功</p>
            <p v-else-if="report?.opState === false" class="config-state is-error">操作失败</p>
            <table class="config-form-table domain-group-table">
              <thead>
                <tr>
                  <th class="config-title-cell" colspan="3">
                    <h2>机器分组配置</h2>
                  </th>
                </tr>
                <tr>
                  <th>项目组</th>
                  <th>组</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in report?.domainGroupRows || []" :key="item.domain">
                  <td>{{ item.domain }}</td>
                  <td>
                    <span v-for="group in item.groups" :key="`${item.domain}-${group}`" class="inline-token">
                      {{ group }}
                    </span>
                  </td>
                  <td class="action-cell">
                    <a class="icon-action" title="编辑" :href="configUrl({ op: 'domainGroupConfigUpdate', domain: item.domain })">
                      <Pencil />
                    </a>
                    <a
                      class="icon-action is-danger"
                      title="删除"
                      :href="legacyConfigUrl('domainGroupConfigDelete', { domain: item.domain, vue: 'true' })"
                      @click="confirmDelete"
                    >
                      <Trash2 />
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section v-else-if="isDomainGroupEdit" class="config-project-form">
            <table class="config-form-table domain-group-editor">
              <tbody>
                <tr>
                  <td>项目组</td>
                  <td>
                    <input v-model="groupDomainInput" :readonly="Boolean(currentParams.get('domain'))" class="wide">
                  </td>
                  <td class="center">
                    <button class="config-primary" type="button" @click="addDomainGroupRow">
                      <Plus class="button-icon" />
                      添加组
                    </button>
                  </td>
                </tr>
                <tr v-for="(row, index) in editableGroupRows" :key="row.key">
                  <td>
                    <input v-model="row.id" :readonly="row.readonly" placeholder="Enter group ...">
                  </td>
                  <td>
                    <div class="tag-editor">
                      <span v-for="ip in row.ips" :key="`${row.key}-${ip}`" class="tag-chip">
                        {{ ip }}
                        <button type="button" title="移除" @click="removeGroupIp(index, ip)">x</button>
                      </span>
                      <input
                        v-model="row.pendingIp"
                        placeholder="Enter ip ..."
                        @blur="addPendingIp(index)"
                        @keydown.enter.prevent="addPendingIp(index)"
                      >
                    </div>
                  </td>
                  <td class="center">
                    <button class="config-danger" type="button" @click="removeDomainGroupRow(index)">
                      <Trash2 class="button-icon" />
                      删除
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="config-submit-row">
              <button class="config-primary" type="button" @click="submitDomainGroup">提交</button>
            </div>
          </section>
          <section v-else-if="isBusinessList" class="config-project-form">
            <form class="business-query" @submit.prevent="queryBusinessDomain">
              <span>Domain</span>
              <el-autocomplete
                v-model="businessDomainInput"
                class="business-domain-input"
                :fetch-suggestions="searchBusinessDomains"
                value-key="value"
                clearable
                @select="selectBusinessDomain"
              />
              <button class="config-primary" type="submit">查询</button>
            </form>
            <p class="config-state is-error">业务大盘标签会默认进行基线告警</p>
            <p v-if="report?.opState === 'Success'" class="config-state">操作成功</p>
            <p v-else-if="report?.opState === 'Fail' || report?.opState === 'Failure'" class="config-state is-error">操作失败</p>
            <div class="business-table-wrap">
              <table class="config-form-table business-config-table">
                <thead>
                  <tr>
                    <th class="config-title-cell" colspan="11">
                      <h2>业务监控配置</h2>
                    </th>
                  </tr>
                  <tr>
                    <th>项目</th>
                    <th>显示顺序</th>
                    <th>敏感数据</th>
                    <th>是否告警</th>
                    <th>BusinessKey</th>
                    <th>标题</th>
                    <th>标签</th>
                    <th>次数</th>
                    <th>平均值</th>
                    <th>总和</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in businessRows" :key="`${item.custom ? 'custom' : 'normal'}-${item.id}`">
                    <td>{{ currentDomain }}</td>
                    <td>{{ item.viewOrder }}</td>
                    <td><span :class="{ 'danger-text': item.privilege }">{{ item.privilege ? '是' : '否' }}</span></td>
                    <td><span :class="{ 'danger-text': item.alarm }">{{ item.alarm ? '是' : '否' }}</span></td>
                    <td class="break-cell">{{ item.id }}</td>
                    <td class="break-cell">{{ item.title }}</td>
                    <td>
                      <span v-for="tag in item.tags" :key="`${item.id}-${tag}`" class="inline-token">{{ tag }}</span>
                    </td>
                    <td class="metric-cell">
                      <span v-if="item.showCount" class="dashboard-mark"></span>
                      <a v-if="!item.custom" class="mini-action" :href="businessAlertUrl(item.id, 'COUNT')">告警</a>
                    </td>
                    <td class="metric-cell">
                      <span v-if="item.showAvg" class="dashboard-mark"></span>
                      <a class="mini-action" :href="businessAlertUrl(item.id, 'AVG')">告警</a>
                    </td>
                    <td class="metric-cell">
                      <span v-if="item.showSum" class="dashboard-mark"></span>
                      <a v-if="!item.custom" class="mini-action" :href="businessAlertUrl(item.id, 'SUM')">告警</a>
                    </td>
                    <td class="action-cell">
                      <a class="icon-action" title="编辑" :href="businessEditUrl(item)">
                        <Pencil />
                      </a>
                      <a class="icon-action is-danger" title="删除" :href="businessDeleteUrl(item)" @click="confirmDelete">
                        <Trash2 />
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
          <form
            v-else-if="isBusinessCustomAdd"
            class="config-project-form"
            method="post"
            :action="businessSubmitUrl('customAddSubmit')"
          >
            <input type="hidden" name="vue" value="true">
            <table class="config-form-table business-custom-form">
              <tbody>
                <tr>
                  <td>项目名称</td>
                  <td><input :value="currentDomain" readonly required></td>
                  <td>BusinessKey</td>
                  <td>
                    <input
                      name="customConfig.id"
                      :value="businessCustomConfig.id"
                      :readonly="Boolean(businessCustomConfig.id)"
                      required
                    >
                  </td>
                </tr>
                <tr>
                  <td>显示标题</td>
                  <td><input name="customConfig.title" :value="businessCustomConfig.title" required></td>
                  <td>显示顺序(数字)</td>
                  <td><input name="customConfig.viewOrder" :value="businessCustomConfig.viewOrder" required></td>
                </tr>
                <tr>
                  <td>是否告警</td>
                  <td>
                    <label class="config-radio">
                      <input type="radio" name="customConfig.alarm" value="true" :checked="businessCustomConfig.alarm">
                      是
                    </label>
                    <label class="config-radio">
                      <input type="radio" name="customConfig.alarm" value="false" :checked="!businessCustomConfig.alarm">
                      否
                    </label>
                  </td>
                  <td>是否为敏感数据</td>
                  <td>
                    <label class="config-radio">
                      <input type="radio" name="customConfig.privilege" value="true" :checked="businessCustomConfig.privilege">
                      是
                    </label>
                    <label class="config-radio">
                      <input type="radio" name="customConfig.privilege" value="false" :checked="!businessCustomConfig.privilege">
                      否
                    </label>
                  </td>
                </tr>
                <tr>
                  <td>规则配置</td>
                  <td colspan="3">
                    <textarea name="customConfig.pattern" :value="businessCustomConfig.pattern" required></textarea>
                  </td>
                </tr>
                <tr>
                  <td class="warning">填写提示:</td>
                  <td colspan="3" class="warning">
                    支持跨项目的指标进行四则运算。使用 ${domain,key,type} 来表示一个特定指标，例如 ${cat,test,COUNT}表示cat项目下，BusinessKey为test的指标的次数。type种类包括COUNT,AVG,SUM。
                  </td>
                </tr>
                <tr>
                  <td colspan="4" class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isBusinessTagConfig"
            class="config-project-form config-xml-form"
            method="post"
            :action="businessSubmitUrl('tagConfig')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="report?.opState === 'Success'" class="config-state">操作成功</p>
            <p v-else-if="report?.opState === 'Fail' || report?.opState === 'Failure'" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>业务标签配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isHeartbeatDisplayPolicy"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('displayPolicy')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>心跳报表展示</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isAlertPolicy"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('alertPolicy')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>告警策略配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isAlertDefaultReceivers"
            id="alertDefaultReceiversForm"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('alertDefaultReceivers')"
          >
            <input type="hidden" name="vue" value="true">
            <input type="hidden" name="allOnOrOff" :value="alertDefaultReceiversMode">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>默认告警人配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center config-action-group">
                    <button class="config-primary" type="submit" @click="alertDefaultReceiversMode = ''">提交</button>
                    <button class="config-primary" type="button" @click="submitAlertDefaultReceivers('on')">全部告警</button>
                    <button class="config-danger button-like" type="button" @click="submitAlertDefaultReceivers('off')">暂停告警</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isAlertSenderConfig"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('alertSenderConfigUpdate')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>告警发送服务配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isServerConfig"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('serverConfigUpdate')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>服务端配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <section class="config-tip-box">
                      <h3>配置说明：</h3>
                      <p>* local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;</p>
                      <p>* hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；</p>
                      <p>* job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为false；</p>
                      <p>* alarm-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为false；</p>
                      <p>* storage : 定义数据存储配置信息</p>
                      <p>* local-report-storage-time : 定义本地报告存放时长，单位为（天）</p>
                      <p>* local-logivew-storage-time : 定义本地日志存放时长，单位为（天）</p>
                      <p>* local-base-dir : 定义本地数据存储目录</p>
                      <p>* hdfs : 定义HDFS配置信息，便于直接登录系统</p>
                      <p>* server-uri : 定义HDFS服务地址</p>
                      <p>* remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）</p>
                    </section>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isSampleConfig"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('sampleConfigUpdate')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>消息采样配置</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form
            v-else-if="isRouterConfig"
            class="config-project-form config-xml-form"
            method="post"
            :action="configSubmitUrl('routerConfigUpdate')"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>客户端路由</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center config-action-group">
                    <button class="config-primary" type="submit">提交</button>
                    <a class="config-primary" :href="routerBuildUrl()" target="_blank" @click="confirmRouterBuild">
                      重算路由
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <section v-else-if="isAlertRule" class="config-project-form">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table transaction-rule-table">
              <thead>
                <tr>
                  <th class="config-title-cell" colspan="6">
                    <h2>{{ alertRuleTitle }}</h2>
                  </th>
                </tr>
                <tr>
                  <th>项目组</th>
                  <th>Type</th>
                  <th>Name</th>
                  <th>监控项</th>
                  <th>是否告警</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in alertRules" :key="item.id">
                  <td>{{ item.domain }}</td>
                  <td>{{ item.type }}</td>
                  <td>{{ item.name }}</td>
                  <td>{{ monitorLabel(item.monitor) }}</td>
                  <td>
                    <span :class="{ 'danger-text': item.available }">{{ item.available ? '是' : '否' }}</span>
                  </td>
                  <td class="action-cell">
                    <a class="icon-action" title="编辑" :href="alertRuleUpdateUrl(item.id)">
                      <Pencil />
                    </a>
                    <a
                      class="icon-action is-danger"
                      title="删除"
                      :href="alertRuleDeleteUrl(item.id)"
                      @click="confirmDelete"
                    >
                      <Trash2 />
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section v-else-if="isAlertRuleUpdate" class="config-project-form transaction-rule-editor">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="2">
                    <h2>{{ alertRuleEditTitle }}</h2>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <div class="transaction-base-row">
                      <label>
                        项目
                        <input v-model="alertRuleForm.domain" :disabled="isEditingAlertRule" required>
                      </label>
                      <label>
                        Type
                        <input v-model="alertRuleForm.type" :disabled="isEditingAlertRule" required>
                      </label>
                      <label>
                        Name
                        <input v-model="alertRuleForm.name" :disabled="isEditingAlertRule" placeholder="All">
                      </label>
                      <label>
                        监控项
                        <select v-model="alertRuleForm.monitor" :disabled="isEditingAlertRule">
                          <option v-for="option in alertRuleMonitorOptions" :key="option.value" :value="option.value">
                            {{ option.label }}
                          </option>
                        </select>
                      </label>
                      <span class="transaction-radio-group">
                        是否告警
                        <label class="config-radio">
                          <input v-model="alertRuleForm.available" type="radio" :value="true">
                          是
                        </label>
                        <label class="config-radio">
                          <input v-model="alertRuleForm.available" type="radio" :value="false">
                          否
                        </label>
                      </span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <div class="rule-config-list">
                      <article v-for="(config, configIndex) in alertRuleConfigs" :key="configIndex" class="rule-config-card">
                        <header class="rule-config-header">
                          <strong>监控规则配置</strong>
                          <button class="config-primary" type="button" @click="addAlertRuleConfig">
                            <Plus class="button-icon" />
                            添加监控规则
                          </button>
                        </header>
                        <div class="rule-config-time">
                          <label>
                            监控开始时间：
                            <input v-model="config.starttime" placeholder="格式如 00:00">
                          </label>
                          <label>
                            监控结束时间：
                            <input v-model="config.endtime" placeholder="格式如 24:00">
                          </label>
                        </div>
                        <section
                          v-for="(condition, conditionIndex) in config.conditions"
                          :key="conditionIndex"
                          class="rule-condition-card"
                        >
                          <h3>监控条件</h3>
                          <div class="rule-condition-row">
                            <label>
                              持续分钟：
                              <input v-model="condition.minute" class="small-input">
                            </label>
                            <label>
                              告警级别：
                              <select v-model="condition.alertType">
                                <option value="warning">warning</option>
                                <option value="error">error</option>
                              </select>
                            </label>
                          </div>
                          <p class="subcondition-title">子条件<span>【必须全部满足才触发告警】</span></p>
                          <div class="subcondition-list">
                            <div
                              v-for="(subCondition, subConditionIndex) in condition.subConditions"
                              :key="subConditionIndex"
                              class="subcondition-row"
                              :class="{ 'is-user-defined': subCondition.type === 'UserDefine' }"
                            >
                              <template v-if="subCondition.type === 'UserDefine'">
                                <input v-model="subCondition.type" type="hidden">
                                <textarea v-model="subCondition.text" rows="8"></textarea>
                              </template>
                              <template v-else>
                                <label>
                                  规则类型：
                                  <select v-model="subCondition.type">
                                    <option
                                      v-for="option in alertRuleTypeOptions"
                                      :key="option.value"
                                      :value="option.value"
                                    >
                                      {{ option.label }}
                                    </option>
                                  </select>
                                </label>
                                <label>
                                  阈值：
                                  <input v-model="subCondition.text" class="small-input">
                                </label>
                              </template>
                              <button
                                class="config-danger button-like"
                                type="button"
                                @click="removeAlertRuleSubCondition(configIndex, conditionIndex, subConditionIndex)"
                              >
                                <Trash2 class="button-icon" />
                                删除子条件
                              </button>
                            </div>
                          </div>
                          <div class="rule-button-row">
                            <button
                              class="config-primary"
                              type="button"
                              :disabled="hasUserDefinedRule(condition)"
                              @click="addAlertRuleSubCondition(configIndex, conditionIndex)"
                            >
                              <Plus class="button-icon" />
                              添加子条件
                            </button>
                            <button class="config-danger button-like" type="button" @click="removeAlertRuleCondition(configIndex, conditionIndex)">
                              <Trash2 class="button-icon" />
                              删除监控条件
                            </button>
                            <button
                              class="config-secondary"
                              type="button"
                              :disabled="hasUserDefinedRule(condition)"
                              @click="useUserDefinedRule(configIndex, conditionIndex)"
                            >
                              自定义监控规则
                            </button>
                          </div>
                        </section>
                        <div class="rule-button-row">
                          <button class="config-primary" type="button" @click="addAlertRuleCondition(configIndex)">
                            <Plus class="button-icon" />
                            添加监控条件
                          </button>
                          <button class="config-danger button-like" type="button" @click="removeAlertRuleConfig(configIndex)">
                            <Trash2 class="button-icon" />
                            删除监控规则
                          </button>
                        </div>
                      </article>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="center">
                    <button class="config-primary" type="button" @click="submitAlertRule">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section v-else-if="isHeartbeatRuleList" class="config-project-form">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table heartbeat-rule-table">
              <thead>
                <tr>
                  <th class="config-title-cell" colspan="5">
                    <h2>心跳告警配置</h2>
                  </th>
                </tr>
                <tr>
                  <th>规则id</th>
                  <th>项目配置</th>
                  <th>指标配置</th>
                  <th>是否告警</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in report?.heartbeatRules || []" :key="item.id">
                  <td class="break-cell">{{ item.id }}</td>
                  <td class="break-cell">{{ item.productlineText }}</td>
                  <td>{{ item.metricText }}</td>
                  <td><span :class="{ 'danger-text': item.available }">{{ item.available ? '是' : '否' }}</span></td>
                  <td class="action-cell">
                    <a class="icon-action" title="编辑" :href="configUrl({ op: 'heartbeatRuleUpdate', key: item.id, domain: currentDomain })">
                      <Pencil />
                    </a>
                    <a class="icon-action is-danger" title="删除" :href="heartbeatRuleDeleteUrl(item.id)" @click="confirmDelete">
                      <Trash2 />
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section v-else-if="isHeartbeatRuleUpdate" class="config-project-form transaction-rule-editor heartbeat-rule-editor">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="2">
                    <h2>编辑心跳告警规则</h2>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <div class="transaction-base-row">
                      <label>
                        项目
                        <input v-model="heartbeatRuleForm.domain" :disabled="isEditingHeartbeatRule" required>
                      </label>
                      <label>
                        指标
                        <select v-model="heartbeatRuleForm.metric" :disabled="isEditingHeartbeatRule">
                          <option v-for="metric in heartbeatExtensionMetrics" :key="metric" :value="metric">{{ metric }}</option>
                        </select>
                      </label>
                      <span class="transaction-radio-group">
                        是否告警
                        <label class="config-radio">
                          <input v-model="heartbeatRuleForm.available" type="radio" :value="true">
                          是
                        </label>
                        <label class="config-radio">
                          <input v-model="heartbeatRuleForm.available" type="radio" :value="false">
                          否
                        </label>
                      </span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <section class="heartbeat-metric-card">
                      <header class="rule-config-header">
                        <strong>匹配对象</strong>
                        <button class="config-primary" type="button" @click="addHeartbeatMetricItem">
                          <Plus class="button-icon" />
                          添加匹配对象
                        </button>
                      </header>
                      <div class="heartbeat-metric-list">
                        <div v-for="(item, index) in heartbeatMetricItems" :key="index" class="heartbeat-metric-row">
                          <label>
                            项目：
                            <textarea v-model="item.productText" placeholder="支持正则,为空即为全局规则"></textarea>
                          </label>
                          <label>
                            指标：
                            <select v-model="item.metricItemText">
                              <option v-for="metric in heartbeatExtensionMetrics" :key="metric" :value="metric">{{ metric }}</option>
                            </select>
                          </label>
                          <button class="config-danger button-like" type="button" @click="removeHeartbeatMetricItem(index)">
                            <Trash2 class="button-icon" />
                            删除
                          </button>
                        </div>
                      </div>
                    </section>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <div class="rule-config-list">
                      <article v-for="(config, configIndex) in alertRuleConfigs" :key="configIndex" class="rule-config-card">
                        <header class="rule-config-header">
                          <strong>监控规则配置</strong>
                          <button class="config-primary" type="button" @click="addAlertRuleConfig">
                            <Plus class="button-icon" />
                            添加监控规则
                          </button>
                        </header>
                        <div class="rule-config-time">
                          <label>
                            监控开始时间：
                            <input v-model="config.starttime" placeholder="格式如 00:00">
                          </label>
                          <label>
                            监控结束时间：
                            <input v-model="config.endtime" placeholder="格式如 24:00">
                          </label>
                        </div>
                        <section
                          v-for="(condition, conditionIndex) in config.conditions"
                          :key="conditionIndex"
                          class="rule-condition-card"
                        >
                          <h3>监控条件</h3>
                          <div class="rule-condition-row">
                            <label>
                              持续分钟：
                              <input v-model="condition.minute" class="small-input">
                            </label>
                            <label>
                              告警级别：
                              <select v-model="condition.alertType">
                                <option value="warning">warning</option>
                                <option value="error">error</option>
                              </select>
                            </label>
                          </div>
                          <p class="subcondition-title">子条件<span>【必须全部满足才触发告警】</span></p>
                          <div class="subcondition-list">
                            <div
                              v-for="(subCondition, subConditionIndex) in condition.subConditions"
                              :key="subConditionIndex"
                              class="subcondition-row"
                              :class="{ 'is-user-defined': subCondition.type === 'UserDefine' }"
                            >
                              <template v-if="subCondition.type === 'UserDefine'">
                                <input v-model="subCondition.type" type="hidden">
                                <textarea v-model="subCondition.text" rows="8"></textarea>
                              </template>
                              <template v-else>
                                <label>
                                  规则类型：
                                  <select v-model="subCondition.type">
                                    <option
                                      v-for="option in alertRuleTypeOptions"
                                      :key="option.value"
                                      :value="option.value"
                                    >
                                      {{ option.label }}
                                    </option>
                                  </select>
                                </label>
                                <label>
                                  阈值：
                                  <input v-model="subCondition.text" class="small-input">
                                </label>
                              </template>
                              <button
                                class="config-danger button-like"
                                type="button"
                                @click="removeAlertRuleSubCondition(configIndex, conditionIndex, subConditionIndex)"
                              >
                                <Trash2 class="button-icon" />
                                删除子条件
                              </button>
                            </div>
                          </div>
                          <div class="rule-button-row">
                            <button
                              class="config-primary"
                              type="button"
                              :disabled="hasUserDefinedRule(condition)"
                              @click="addAlertRuleSubCondition(configIndex, conditionIndex)"
                            >
                              <Plus class="button-icon" />
                              添加子条件
                            </button>
                            <button class="config-danger button-like" type="button" @click="removeAlertRuleCondition(configIndex, conditionIndex)">
                              <Trash2 class="button-icon" />
                              删除监控条件
                            </button>
                            <button
                              class="config-secondary"
                              type="button"
                              :disabled="hasUserDefinedRule(condition)"
                              @click="useUserDefinedRule(configIndex, conditionIndex)"
                            >
                              自定义监控规则
                            </button>
                          </div>
                        </section>
                        <div class="rule-button-row">
                          <button class="config-primary" type="button" @click="addAlertRuleCondition(configIndex)">
                            <Plus class="button-icon" />
                            添加监控条件
                          </button>
                          <button class="config-danger button-like" type="button" @click="removeAlertRuleConfig(configIndex)">
                            <Trash2 class="button-icon" />
                            删除监控规则
                          </button>
                        </div>
                      </article>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="center">
                    <button class="config-primary" type="button" @click="submitHeartbeatRule">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section v-else-if="isExceptionList" class="config-project-form exception-config-view">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <div class="exception-tabs">
              <a :class="{ 'is-active': exceptionTab === 'threshold' }" :href="configUrl({ op: 'exception', type: 'threshold' })">异常阈值</a>
              <a :class="{ 'is-active': exceptionTab === 'exclude' }" :href="configUrl({ op: 'exception', type: 'exclude' })">异常过滤</a>
            </div>
            <section v-if="exceptionTab === 'threshold'">
              <table class="config-form-table exception-table">
                <thead>
                  <tr>
                    <th class="config-title-cell" colspan="6">
                      <h2>异常阈值配置</h2>
                    </th>
                  </tr>
                  <tr>
                    <th>域名</th>
                    <th>异常名称</th>
                    <th>Warning阈值</th>
                    <th>Error阈值</th>
                    <th>是否告警</th>
                    <th>
                      操作
                      <a class="mini-add-action" title="添加" :href="configUrl({ op: 'exceptionThresholdAdd', type: 'threshold' })">
                        <Plus />
                      </a>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in report?.exceptionLimits || []" :key="item.id || `${item.domain}:${item.name}`">
                    <td>{{ item.domain }}</td>
                    <td>{{ item.name }}</td>
                    <td>{{ item.warning }}</td>
                    <td>{{ item.error }}</td>
                    <td><span :class="{ 'danger-text': item.available }">{{ item.available ? '是' : '否' }}</span></td>
                    <td class="action-cell">
                      <a
                        class="icon-action"
                        title="编辑"
                        :href="configUrl({ op: 'exceptionThresholdUpdate', domain: item.domain, exception: item.name, type: 'threshold' })"
                      >
                        <Pencil />
                      </a>
                      <a
                        class="icon-action is-danger"
                        title="删除"
                        :href="exceptionThresholdDeleteUrl(item)"
                        @click="confirmDelete"
                      >
                        <Trash2 />
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </section>
            <section v-else>
              <table class="config-form-table exception-table exception-exclude-table">
                <thead>
                  <tr>
                    <th class="config-title-cell" colspan="3">
                      <h2>异常过滤配置</h2>
                    </th>
                  </tr>
                  <tr>
                    <th>域名</th>
                    <th>异常名称</th>
                    <th>
                      <a class="mini-add-action" title="添加" :href="configUrl({ op: 'exceptionExcludeAdd', type: 'exclude' })">
                        <Plus />
                      </a>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in report?.exceptionExcludes || []" :key="item.id || `${item.domain}:${item.name}`">
                    <td>{{ item.domain }}</td>
                    <td>{{ item.name }}</td>
                    <td class="action-cell">
                      <a
                        class="icon-action is-danger"
                        title="删除"
                        :href="exceptionExcludeDeleteUrl(item)"
                        @click="confirmDelete"
                      >
                        <Trash2 />
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </section>
          </section>
          <form
            v-else-if="isExceptionThresholdEdit"
            class="config-project-form exception-edit-form"
            method="post"
            :action="configSubmitUrl('exceptionThresholdUpdateSubmit', { type: 'threshold' })"
          >
            <input type="hidden" name="vue" value="true">
            <input type="hidden" name="type" value="threshold">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="2">
                    <h2>修改异常阈值配置信息</h2>
                  </td>
                </tr>
                <tr>
                  <td>项目名称</td>
                  <td>
                    <input
                      name="exceptionLimit.domain"
                      :value="exceptionLimitForm.domain"
                      :readonly="Boolean(exceptionLimitForm.domain)"
                      list="exception-domain-list"
                      placeholder="input domain for search"
                      required
                    >
                  </td>
                </tr>
                <tr>
                  <td>异常名称</td>
                  <td>
                    <input
                      name="exceptionLimit.name"
                      :value="exceptionLimitForm.name"
                      :readonly="Boolean(exceptionLimitForm.name)"
                      list="exception-name-list"
                      placeholder="input exception for search"
                      required
                    >
                  </td>
                </tr>
                <tr>
                  <td>warning阈值</td>
                  <td><input name="exceptionLimit.warning" type="number" :value="exceptionLimitForm.warning" required></td>
                </tr>
                <tr>
                  <td>error阈值</td>
                  <td><input name="exceptionLimit.error" type="number" :value="exceptionLimitForm.error" required></td>
                </tr>
                <tr>
                  <td>是否告警</td>
                  <td>
                    <label class="config-radio">
                      <input type="radio" name="exceptionLimit.available" value="true" :checked="exceptionLimitForm.available">
                      是
                    </label>
                    <label class="config-radio">
                      <input type="radio" name="exceptionLimit.available" value="false" :checked="!exceptionLimitForm.available">
                      否
                    </label>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <datalist id="exception-domain-list">
              <option v-for="item in report?.domainList || []" :key="item" :value="item"></option>
            </datalist>
            <datalist id="exception-name-list">
              <option v-for="item in report?.exceptionList || []" :key="item" :value="item"></option>
            </datalist>
          </form>
          <form
            v-else-if="isExceptionExcludeAdd"
            class="config-project-form exception-edit-form"
            method="post"
            :action="configSubmitUrl('exceptionExcludeUpdateSubmit', { type: 'exclude' })"
          >
            <input type="hidden" name="vue" value="true">
            <input type="hidden" name="type" value="exclude">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="2">
                    <h2>修改异常过滤配置信息</h2>
                  </td>
                </tr>
                <tr>
                  <td>项目名称</td>
                  <td>
                    <input name="exceptionExclude.domain" list="exception-domain-list" placeholder="input domain for search" required>
                  </td>
                </tr>
                <tr>
                  <td>异常名称</td>
                  <td>
                    <input name="exceptionExclude.name" list="exception-name-list" placeholder="input exception for search" required>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <datalist id="exception-domain-list">
              <option v-for="item in report?.domainList || []" :key="item" :value="item"></option>
            </datalist>
            <datalist id="exception-name-list">
              <option v-for="item in report?.exceptionList || []" :key="item" :value="item"></option>
            </datalist>
          </form>
          <form v-else-if="isProjectAdd" class="config-project-form" method="get" :action="legacyConfigUrl()" @submit="validateProjectAdd">
            <input type="hidden" name="op" value="updateSubmit">
            <input type="hidden" name="vue" value="true">
            <input type="hidden" name="project.cmdbDomain" value="default">
            <input type="hidden" name="project.level" value="1">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="3">
                    <h2>添加项目</h2>
                  </td>
                </tr>
                <tr>
                  <td>CAT上项目名称</td>
                  <td><input name="project.domain" pattern="[A-Za-z0-9][A-Za-z0-9.-]*" autofocus></td>
                  <td class="warning">注意：建议使用半角英文和半角符号(. -)。</td>
                </tr>
                <tr>
                  <td>事业部</td>
                  <td><input name="project.bu"></td>
                  <td>所属部门名称</td>
                </tr>
                <tr>
                  <td>产品线</td>
                  <td><input name="project.cmdbProductline"></td>
                  <td>所属产品线名称</td>
                </tr>
                <tr>
                  <td>负责人</td>
                  <td><input name="project.owner"></td>
                  <td>项目负责人</td>
                </tr>
                <tr>
                  <td>项目组邮件</td>
                  <td><input class="wide" name="project.email"></td>
                  <td>字段，多个以逗号分隔</td>
                </tr>
                <tr>
                  <td>项目组号码</td>
                  <td><input class="wide" name="project.phone"></td>
                  <td>字段，多个以逗号分隔</td>
                </tr>
                <tr>
                  <td colspan="3" class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <form v-else-if="project" class="config-project-form" method="get" :action="legacyConfigUrl()">
            <input type="hidden" name="project.id" :value="project.id">
            <input type="hidden" name="project.domain" :value="project.domain">
            <input type="hidden" name="project.cmdbDomain" :value="project.cmdbDomain">
            <input type="hidden" name="project.level" :value="project.level">
            <input type="hidden" name="op" value="updateSubmit">
            <input type="hidden" name="vue" value="true">
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td class="config-title-cell" colspan="3">
                    <h2>项目基本信息</h2>
                  </td>
                </tr>
                <tr>
                  <td>CAT上项目名称</td>
                  <td>{{ project.domain }}</td>
                  <td class="warning">注意：建议使用半角英文和半角符号(. -)。</td>
                </tr>
                <tr>
                  <td>事业部</td>
                  <td><input name="project.bu" :value="project.bu"></td>
                  <td>所属部门名称</td>
                </tr>
                <tr>
                  <td>产品线</td>
                  <td><input name="project.cmdbProductline" :value="project.cmdbProductline"></td>
                  <td>所属产品线名称</td>
                </tr>
                <tr>
                  <td>负责人</td>
                  <td><input name="project.owner" :value="project.owner"></td>
                  <td>项目负责人</td>
                </tr>
                <tr>
                  <td>项目组邮件</td>
                  <td><input class="wide" name="project.email" :value="project.email"></td>
                  <td>字段，多个以逗号分隔</td>
                </tr>
                <tr>
                  <td>项目组号码</td>
                  <td><input class="wide" name="project.phone" :value="project.phone"></td>
                  <td>字段，多个以逗号分隔</td>
                </tr>
                <tr>
                  <td colspan="3" class="center">
                    <button class="config-primary" type="submit">更新</button>
                    <a class="config-danger" :href="legacyConfigUrl('projectDelete', { projectId: String(project.id), vue: 'true' })">删除</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
          <section v-else class="empty-state">没有找到项目配置。</section>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import ConfigSidebar from '../components/ConfigSidebar.vue'
import TopbarUser from '../components/TopbarUser.vue'
import XmlEditor from '../components/XmlEditor.vue'

interface Project {
  bu: string
  cmdbDomain: string
  cmdbProductline: string
  domain: string
  email: string
  id: number
  level: number
  owner: string
  phone: string
}

interface ConfigReport {
  contextPath: string
  actionName: string
  content: string
  configs: BusinessConfigItem[]
  customConfig: BusinessCustomConfig | null
  customConfigs: BusinessConfigItem[]
  domain: string
  domainGroupRows: DomainGroupRow[]
  domains: string[]
  domainList: string[]
  eventRuleAvailable: boolean | null
  eventRuleConfigs: string
  eventRuleId: string
  eventRules: AlertRule[]
  heartbeatExtensionMetrics: string[]
  heartbeatRuleAvailable: boolean | null
  heartbeatRuleConfigs: string
  heartbeatRuleId: string
  heartbeatRuleMetrics: string
  heartbeatRules: HeartbeatRule[]
  exceptionExclude: ExceptionExclude | null
  exceptionExcludes: ExceptionExclude[]
  exceptionLimit: ExceptionLimit | null
  exceptionLimits: ExceptionLimit[]
  exceptionList: string[]
  groupRows: GroupRow[]
  opState: boolean | string | null
  project: Project | null
  projectAdd: boolean
  projects: Project[]
  transactionRuleAvailable: boolean | null
  transactionRuleConfigs: string
  transactionRuleId: string
  transactionRules: AlertRule[]
}

interface DomainGroupRow {
  domain: string
  groups: string[]
}

interface GroupRow {
  id: string
  ips: string[]
}

interface EditableGroupRow extends GroupRow {
  key: number
  pendingIp: string
  readonly: boolean
}

interface BusinessConfigItem {
  alarm: boolean
  custom: boolean
  id: string
  privilege: boolean
  showAvg: boolean
  showCount: boolean
  showSum: boolean
  tags: string[]
  title: string
  viewOrder: number
}

interface BusinessCustomConfig {
  alarm: boolean
  id: string
  pattern: string
  privilege: boolean
  title: string
  viewOrder: number
}

interface AlertRule {
  available: boolean
  domain: string
  id: string
  monitor: string
  name: string
  type: string
}

interface AlertRuleForm {
  available: boolean
  domain: string
  monitor: string
  name: string
  type: string
}

interface ExceptionLimit {
  available: boolean
  domain: string
  error: number
  id: string
  name: string
  warning: number
}

interface ExceptionExclude {
  domain: string
  id: string
  name: string
}

interface HeartbeatRule {
  available: boolean
  id: string
  metricText: string
  productlineText: string
}

interface HeartbeatRuleForm {
  available: boolean
  domain: string
  metric: string
}

interface HeartbeatMetricItem {
  metricItemText: string
  productText: string
}

interface RuleConfigData {
  conditions?: RuleConditionData[]
  endtime?: string
  starttime?: string
}

interface RuleConditionData {
  alertType?: string
  minute?: number | string
  'sub-conditions'?: RuleSubConditionData[]
}

interface RuleSubConditionData {
  text?: string
  type?: string
}

interface EditableRuleConfig {
  conditions: EditableRuleCondition[]
  endtime: string
  starttime: string
}

interface EditableRuleCondition {
  alertType: string
  minute: string
  subConditions: EditableSubCondition[]
}

interface EditableSubCondition {
  text: string
  type: string
}

const report = ref<ConfigReport | null>(null)
const domainInput = ref('')
const businessDomainInput = ref('')
const groupDomainInput = ref('')
const editableGroupRows = ref<EditableGroupRow[]>([])
const alertRuleForm = ref<AlertRuleForm>(defaultAlertRuleForm('cat'))
const alertRuleConfigs = ref<EditableRuleConfig[]>([defaultRuleConfig()])
const heartbeatRuleForm = ref<HeartbeatRuleForm>(defaultHeartbeatRuleForm('cat', ''))
const heartbeatMetricItems = ref<HeartbeatMetricItem[]>([defaultHeartbeatMetricItem('')])
const xmlEditorContent = ref('')
const alertDefaultReceiversMode = ref('')
const loading = ref(false)
const loadError = ref('')
let groupRowKey = 1

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return report.value?.contextPath || '/cat'
})
const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentAction = computed(() => report.value?.actionName || currentParams.value.get('op') || 'projects')
const currentDomain = computed(() => report.value?.domain || currentParams.value.get('domain') || 'cat')
const isProjectAdd = computed(() => currentParams.value.get('op') === 'projectAdd' || Boolean(report.value?.projectAdd))
const isDomainGroupList = computed(() => currentAction.value === 'domainGroupConfigs')
const isDomainGroupEdit = computed(() => currentAction.value === 'domainGroupConfigUpdate')
const isDomainGroupConfig = computed(() => isDomainGroupList.value || isDomainGroupEdit.value)
const isBusinessList = computed(() => currentAction.value === 'businessList')
const isBusinessCustomAdd = computed(() => currentAction.value === 'businessCustomAdd')
const isBusinessTagConfig = computed(() => currentAction.value === 'businessTagConfig')
const isHeartbeatDisplayPolicy = computed(() => currentAction.value === 'displayPolicy')
const isAlertPolicy = computed(() => currentAction.value === 'alertPolicy')
const isAlertDefaultReceivers = computed(() => currentAction.value === 'alertDefaultReceivers')
const isAlertSenderConfig = computed(() => currentAction.value === 'alertSenderConfigUpdate')
const isServerConfig = computed(() => currentAction.value === 'serverConfigUpdate')
const isSampleConfig = computed(() => currentAction.value === 'sampleConfigUpdate')
const isRouterConfig = computed(() => currentAction.value === 'routerConfigUpdate')
const isTransactionAlertRule = computed(() => currentAction.value === 'transactionRule')
const isEventAlertRule = computed(() => currentAction.value === 'eventRule')
const isAlertRule = computed(() => isTransactionAlertRule.value || isEventAlertRule.value)
const isTransactionAlertRuleUpdate = computed(() => currentAction.value === 'transactionRuleUpdate')
const isEventAlertRuleUpdate = computed(() => currentAction.value === 'eventRuleUpdate')
const isAlertRuleUpdate = computed(() => isTransactionAlertRuleUpdate.value || isEventAlertRuleUpdate.value)
const isExceptionList = computed(() => currentAction.value === 'exception')
const isExceptionThresholdEdit = computed(() => currentAction.value === 'exceptionThresholdUpdate'
  || currentAction.value === 'exceptionThresholdAdd')
const isExceptionExcludeAdd = computed(() => currentAction.value === 'exceptionExcludeAdd')
const isExceptionEdit = computed(() => isExceptionThresholdEdit.value || isExceptionExcludeAdd.value)
const isExceptionConfig = computed(() => isExceptionList.value || isExceptionEdit.value)
const isHeartbeatRuleList = computed(() => currentAction.value === 'heartbeatRuleConfigList')
const isHeartbeatRuleUpdate = computed(() => currentAction.value === 'heartbeatRuleUpdate')
const isHeartbeatRuleConfig = computed(() => isHeartbeatRuleList.value || isHeartbeatRuleUpdate.value)
const isBusinessConfig = computed(() => isBusinessList.value || isBusinessCustomAdd.value || isBusinessTagConfig.value
  || isHeartbeatDisplayPolicy.value)
const isAlertConfig = computed(() => isAlertRule.value || isAlertRuleUpdate.value || isExceptionConfig.value
  || isHeartbeatRuleConfig.value)
const isSystemConfig = computed(() => isAlertPolicy.value || isAlertDefaultReceivers.value
  || isAlertSenderConfig.value || isServerConfig.value || isSampleConfig.value || isRouterConfig.value)
const isProjectConfig = computed(() => !isDomainGroupConfig.value && !isBusinessConfig.value && !isAlertConfig.value
  && !isSystemConfig.value)
const activeSidebarItem = computed(() => {
  if (isDomainGroupConfig.value) {
    return 'domainGroupConfigs'
  }
  if (isBusinessList.value || isBusinessCustomAdd.value) {
    return 'businessList'
  }
  if (isBusinessTagConfig.value) {
    return 'businessTagConfig'
  }
  if (isHeartbeatDisplayPolicy.value) {
    return 'displayPolicy'
  }
  if (isTransactionAlertRule.value || isTransactionAlertRuleUpdate.value) {
    return 'transactionRule'
  }
  if (isEventAlertRule.value || isEventAlertRuleUpdate.value) {
    return 'eventRule'
  }
  if (isExceptionConfig.value) {
    return 'exception'
  }
  if (isHeartbeatRuleConfig.value) {
    return 'heartbeatRuleConfigList'
  }
  if (isProjectConfig.value) {
    return 'projects'
  }
  return currentAction.value
})
const showConfigHeading = computed(() => !isAlertPolicy.value && !isAlertDefaultReceivers.value
  && !isAlertSenderConfig.value && !isServerConfig.value && !isSampleConfig.value && !isRouterConfig.value
  && !isProjectConfig.value && !isDomainGroupList.value && !isBusinessList.value && !isBusinessTagConfig.value
  && !isHeartbeatDisplayPolicy.value && !isAlertConfig.value)
const showConfigActions = computed(() => isProjectAdd.value || isDomainGroupEdit.value || isBusinessCustomAdd.value
  || isAlertRuleUpdate.value || isHeartbeatRuleUpdate.value || isExceptionEdit.value || isDomainGroupList.value
  || isBusinessList.value || isAlertRule.value || isHeartbeatRuleList.value || isProjectConfig.value)
const showProjectSearch = computed(() => !isProjectAdd.value && !isDomainGroupConfig.value && !isBusinessConfig.value
  && !isAlertConfig.value && !isSystemConfig.value)
const businessRows = computed(() => [...(report.value?.configs || []), ...(report.value?.customConfigs || [])])
const businessCustomConfig = computed<BusinessCustomConfig>(() => {
  return report.value?.customConfig || {
    alarm: false,
    id: '',
    pattern: '',
    privilege: false,
    title: '',
    viewOrder: 0
  }
})
const alertRuleKind = computed(() => (isEventAlertRule.value || isEventAlertRuleUpdate.value) ? 'event' : 'transaction')
const alertRuleListAction = computed(() => alertRuleKind.value === 'event' ? 'eventRule' : 'transactionRule')
const alertRuleUpdateAction = computed(() => alertRuleKind.value === 'event' ? 'eventRuleUpdate' : 'transactionRuleUpdate')
const alertRuleSubmitAction = computed(() => alertRuleKind.value === 'event' ? 'eventRuleSubmit' : 'transactionRuleSubmit')
const alertRuleDeleteAction = computed(() => alertRuleKind.value === 'event' ? 'eventRuleDelete' : 'transactionRuleDelete')
const alertRuleTitle = computed(() => alertRuleKind.value === 'event' ? 'Event告警' : 'Transaction告警')
const alertRuleEditTitle = computed(() => alertRuleKind.value === 'event' ? '编辑Event监控规则' : '编辑Transaction监控规则')
const alertRules = computed(() => alertRuleKind.value === 'event' ? (report.value?.eventRules || []) : (report.value?.transactionRules || []))
const alertRuleId = computed(() => {
  return alertRuleKind.value === 'event' ? report.value?.eventRuleId : report.value?.transactionRuleId
})
const alertRuleAvailable = computed(() => {
  return alertRuleKind.value === 'event' ? report.value?.eventRuleAvailable : report.value?.transactionRuleAvailable
})
const alertRuleConfigText = computed(() => {
  return alertRuleKind.value === 'event' ? report.value?.eventRuleConfigs : report.value?.transactionRuleConfigs
})
const isEditingAlertRule = computed(() => Boolean(alertRuleId.value || currentParams.value.get('ruleId')))
const alertRuleMonitorOptions = computed(() => {
  if (alertRuleKind.value === 'event') {
    return [
      { label: '执行次数', value: 'count' },
      { label: '失败率', value: 'failRatio' }
    ]
  }
  return [
    { label: '执行次数', value: 'count' },
    { label: '响应时间', value: 'avg' },
    { label: '失败率', value: 'failRatio' },
    { label: '最大响应时间', value: 'max' }
  ]
})
const alertRuleTypeOptions = [
  { label: '最大值(当前值)', value: 'MaxVal' },
  { label: '最小值(当前值)', value: 'MinVal' },
  { label: '波动上升百分比(当前值)', value: 'FluAscPer' },
  { label: '波动下降百分比(当前值)', value: 'FluDescPer' },
  { label: '总和最大值(当前值)', value: 'SumMaxVal' },
  { label: '总和最小值(当前值)', value: 'SumMinVal' }
]
const exceptionTab = computed(() => currentParams.value.get('type') === 'exclude' ? 'exclude' : 'threshold')
const exceptionLimitForm = computed<ExceptionLimit>(() => {
  return report.value?.exceptionLimit || {
    available: true,
    domain: '',
    error: 0,
    id: '',
    name: '',
    warning: 0
  }
})
const heartbeatExtensionMetrics = computed(() => report.value?.heartbeatExtensionMetrics || [])
const isEditingHeartbeatRule = computed(() => Boolean(report.value?.heartbeatRuleId || currentParams.value.get('key')
  || currentParams.value.get('ruleId')))
const pageTitle = computed(() => {
  if (isHeartbeatDisplayPolicy.value) {
    return '心跳报表展示'
  }
  if (isAlertPolicy.value) {
    return '告警策略'
  }
  if (isAlertDefaultReceivers.value) {
    return '默认告警人'
  }
  if (isAlertSenderConfig.value) {
    return '告警服务端'
  }
  if (isServerConfig.value) {
    return '服务端配置'
  }
  if (isSampleConfig.value) {
    return '消息采样配置'
  }
  if (isRouterConfig.value) {
    return '客户端路由'
  }
  if (isBusinessTagConfig.value) {
    return '业务标签配置'
  }
  if (isBusinessCustomAdd.value) {
    return '修改业务监控规则'
  }
  if (isBusinessList.value) {
    return '业务监控配置'
  }
  if (isAlertRuleUpdate.value) {
    return alertRuleEditTitle.value
  }
  if (isAlertRule.value) {
    return alertRuleTitle.value
  }
  if (isExceptionThresholdEdit.value) {
    return '异常阈值配置'
  }
  if (isExceptionExcludeAdd.value) {
    return '异常过滤配置'
  }
  if (isExceptionList.value) {
    return '异常告警配置'
  }
  if (isHeartbeatRuleUpdate.value) {
    return '编辑心跳告警规则'
  }
  if (isHeartbeatRuleList.value) {
    return '心跳告警配置'
  }
  if (isDomainGroupEdit.value) {
    return '编辑机器分组配置'
  }
  if (isDomainGroupList.value) {
    return '机器分组配置'
  }
  return isProjectAdd.value ? '添加项目' : '项目配置'
})
const project = computed(() => report.value?.project || null)
const projectSuggestions = computed(() => {
  return (report.value?.projects || []).map((item) => ({
    category: `${item.bu || ''} - ${item.cmdbProductline || ''}`,
    label: item.domain,
    value: item.domain
  }))
})
const applicationUrl = computed(() => `${contextPath.value}/mvc/vue/r/t?op=view&domain=${encodeURIComponent(currentDomain.value)}&ip=All`)
const documentUrl = computed(() => `${contextPath.value}/mvc/vue/r/home?op=view&docName=index&domain=${encodeURIComponent(currentDomain.value)}`)

onMounted(() => {
  loadConfig()
})

async function loadConfig() {
  loading.value = true
  loadError.value = ''

  try {
    const response = await fetch(dataUrl(), { headers: { Accept: 'application/json' } })

    if (response.redirected && response.url.includes('/mvc/vue/s/login')) {
      window.location.href = response.url
      return
    }
    if (response.status === 401 && isJsonResponse(response)) {
      const data = await response.json() as { loginUrl?: string; message?: string }

      if (data.loginUrl) {
        window.location.href = data.loginUrl
        return
      }
      throw new Error(data.message || '请先登录后查看项目配置')
    }
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    if (!isJsonResponse(response)) {
      throw new Error('接口未返回 JSON，请确认当前账号是否已登录并具备配置页面权限')
    }
    const data = await response.json() as ConfigReport

    report.value = data
    domainInput.value = data.domain || 'cat'
    businessDomainInput.value = data.domain || 'cat'
    groupDomainInput.value = currentParams.value.get('domain') || data.domain || ''
    xmlEditorContent.value = data.content || ''
    editableGroupRows.value = (data.groupRows || []).map((row) => toEditableGroupRow(row, true))
    initAlertRuleEditor(data)
    initHeartbeatRuleEditor(data)
  } catch (error) {
    loadError.value = `项目配置加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function toEditableGroupRow(row: GroupRow, readonly: boolean): EditableGroupRow {
  return {
    id: row.id,
    ips: [...row.ips],
    key: groupRowKey++,
    pendingIp: '',
    readonly
  }
}

function initAlertRuleEditor(data: ConfigReport) {
  if (!isAlertRuleUpdate.value) {
    return
  }
  const parsedRuleId = parseAlertRuleId(alertRuleId.value || currentParams.value.get('ruleId') || '')

  alertRuleForm.value = {
    available: alertRuleAvailable.value ?? true,
    domain: parsedRuleId.domain || data.domain || currentParams.value.get('domain') || 'cat',
    monitor: parsedRuleId.monitor || 'count',
    name: parsedRuleId.name || 'All',
    type: parsedRuleId.type || ''
  }
  alertRuleConfigs.value = parseAlertRuleConfigs(alertRuleConfigText.value)
}

function initHeartbeatRuleEditor(data: ConfigReport) {
  if (!isHeartbeatRuleUpdate.value) {
    return
  }
  const ruleId = data.heartbeatRuleId || currentParams.value.get('key') || currentParams.value.get('ruleId') || ''
  const parsedRuleId = parseHeartbeatRuleId(ruleId)
  const firstMetric = data.heartbeatExtensionMetrics?.[0] || ''

  heartbeatRuleForm.value = {
    available: data.heartbeatRuleAvailable ?? true,
    domain: parsedRuleId.domain || data.domain || currentParams.value.get('domain') || 'cat',
    metric: parsedRuleId.metric || firstMetric
  }
  alertRuleConfigs.value = parseAlertRuleConfigs(data.heartbeatRuleConfigs)
  heartbeatMetricItems.value = parseHeartbeatMetricItems(data.heartbeatRuleMetrics, firstMetric)
}

function parseHeartbeatRuleId(ruleId: string) {
  const parts = ruleId.split(';')

  return {
    domain: parts[0] || '',
    metric: parts[1] || ''
  }
}

function parseAlertRuleId(ruleId: string) {
  const parts = ruleId.split(';')

  return {
    domain: parts[0] || '',
    type: parts[1] || '',
    name: parts[2] || '',
    monitor: parts[3] || ''
  }
}

function parseAlertRuleConfigs(configText: string | undefined) {
  if (!configText) {
    return [defaultRuleConfig()]
  }
  try {
    const configs = JSON.parse(configText) as RuleConfigData[]
    const editableConfigs = configs.map(toEditableRuleConfig).filter((config) => config.conditions.length > 0)

    return editableConfigs.length > 0 ? editableConfigs : [defaultRuleConfig()]
  } catch (error) {
    console.error('Unable to parse transaction rule configs.', error)
    return [defaultRuleConfig()]
  }
}

function toEditableRuleConfig(config: RuleConfigData): EditableRuleConfig {
  const conditions = (config.conditions || []).map(toEditableRuleCondition).filter((condition) => condition.subConditions.length > 0)

  return {
    conditions: conditions.length > 0 ? conditions : [defaultRuleCondition()],
    endtime: config.endtime || '24:00',
    starttime: config.starttime || '00:00'
  }
}

function toEditableRuleCondition(condition: RuleConditionData): EditableRuleCondition {
  const subConditions = (condition['sub-conditions'] || []).map(toEditableSubCondition)
    .filter((subCondition) => subCondition.type && subCondition.text !== '')

  return {
    alertType: condition.alertType || 'warning',
    minute: condition.minute == null ? '' : String(condition.minute),
    subConditions: subConditions.length > 0 ? subConditions : [defaultSubCondition()]
  }
}

function toEditableSubCondition(subCondition: RuleSubConditionData): EditableSubCondition {
  return {
    text: subCondition.text || '',
    type: subCondition.type || 'MaxVal'
  }
}

function defaultAlertRuleForm(domain: string): AlertRuleForm {
  return {
    available: true,
    domain,
    monitor: 'count',
    name: 'All',
    type: ''
  }
}

function defaultHeartbeatRuleForm(domain: string, metric: string): HeartbeatRuleForm {
  return {
    available: true,
    domain,
    metric
  }
}

function parseHeartbeatMetricItems(metricsText: string | undefined, defaultMetric: string) {
  if (!metricsText) {
    return [defaultHeartbeatMetricItem(defaultMetric)]
  }
  try {
    const items = JSON.parse(metricsText) as HeartbeatMetricItem[]
    const editableItems = items.map((item) => ({
      metricItemText: item.metricItemText || defaultMetric,
      productText: item.productText || ''
    })).filter((item) => item.metricItemText || item.productText)

    return editableItems.length > 0 ? editableItems : [defaultHeartbeatMetricItem(defaultMetric)]
  } catch (error) {
    console.error('Unable to parse heartbeat rule metrics.', error)
    return [defaultHeartbeatMetricItem(defaultMetric)]
  }
}

function defaultHeartbeatMetricItem(metric: string): HeartbeatMetricItem {
  return {
    metricItemText: metric,
    productText: ''
  }
}

function defaultRuleConfig(): EditableRuleConfig {
  return {
    conditions: [defaultRuleCondition()],
    endtime: '24:00',
    starttime: '00:00'
  }
}

function defaultRuleCondition(): EditableRuleCondition {
  return {
    alertType: 'warning',
    minute: '',
    subConditions: [defaultSubCondition()]
  }
}

function defaultSubCondition(): EditableSubCondition {
  return {
    text: '',
    type: 'MaxVal'
  }
}

function addAlertRuleConfig() {
  alertRuleConfigs.value.push(defaultRuleConfig())
}

function removeAlertRuleConfig(configIndex: number) {
  if (alertRuleConfigs.value.length === 1) {
    alertRuleConfigs.value = [defaultRuleConfig()]
    return
  }
  alertRuleConfigs.value.splice(configIndex, 1)
}

function addAlertRuleCondition(configIndex: number) {
  alertRuleConfigs.value[configIndex].conditions.push(defaultRuleCondition())
}

function removeAlertRuleCondition(configIndex: number, conditionIndex: number) {
  const conditions = alertRuleConfigs.value[configIndex].conditions

  if (conditions.length === 1) {
    conditions.splice(0, 1, defaultRuleCondition())
    return
  }
  conditions.splice(conditionIndex, 1)
}

function addAlertRuleSubCondition(configIndex: number, conditionIndex: number) {
  alertRuleConfigs.value[configIndex].conditions[conditionIndex].subConditions.push(defaultSubCondition())
}

function removeAlertRuleSubCondition(configIndex: number, conditionIndex: number, subConditionIndex: number) {
  const subConditions = alertRuleConfigs.value[configIndex].conditions[conditionIndex].subConditions

  if (subConditions.length === 1) {
    subConditions.splice(0, 1, defaultSubCondition())
    return
  }
  subConditions.splice(subConditionIndex, 1)
}

function useUserDefinedRule(configIndex: number, conditionIndex: number) {
  alertRuleConfigs.value[configIndex].conditions[conditionIndex].subConditions = [{
    text: '',
    type: 'UserDefine'
  }]
}

function hasUserDefinedRule(condition: EditableRuleCondition) {
  return condition.subConditions.some((subCondition) => subCondition.type === 'UserDefine')
}

function addHeartbeatMetricItem() {
  heartbeatMetricItems.value.push(defaultHeartbeatMetricItem(heartbeatRuleForm.value.metric || heartbeatExtensionMetrics.value[0] || ''))
}

function removeHeartbeatMetricItem(index: number) {
  if (heartbeatMetricItems.value.length === 1) {
    heartbeatMetricItems.value = [defaultHeartbeatMetricItem(heartbeatRuleForm.value.metric || heartbeatExtensionMetrics.value[0] || '')]
    return
  }
  heartbeatMetricItems.value.splice(index, 1)
}

function submitAlertRule() {
  const domain = alertRuleForm.value.domain.trim()
  const type = alertRuleForm.value.type.trim()
  const name = alertRuleForm.value.name.trim() || 'All'

  if (!domain) {
    window.alert('项目不能为空')
    return
  }
  if (!type) {
    window.alert('Type不能为空')
    return
  }
  let configs = ''

  try {
    configs = alertRuleConfigJson()
  } catch (error) {
    window.alert(error instanceof Error ? error.message : String(error))
    return
  }
  const ruleId = `${domain};${type};${name};${alertRuleForm.value.monitor}`
  const params = new URLSearchParams()

  alertRuleForm.value.name = name
  params.set('op', alertRuleSubmitAction.value)
  params.set('configs', configs)
  params.set('ruleId', ruleId)
  params.set('available', String(alertRuleForm.value.available))
  params.set('domain', domain)
  params.set('vue', 'true')
  window.location.href = `${contextPath.value}/mvc/s/config?${params.toString()}`
}

function submitHeartbeatRule() {
  const domain = heartbeatRuleForm.value.domain.trim()

  if (!domain) {
    window.alert('项目不能为空')
    return
  }
  let configs = ''

  try {
    configs = alertRuleConfigJson()
  } catch (error) {
    window.alert(error instanceof Error ? error.message : String(error))
    return
  }
  const currentRuleId = report.value?.heartbeatRuleId || currentParams.value.get('key') || currentParams.value.get('ruleId') || ''
  const ruleId = currentRuleId || `${domain};${heartbeatRuleForm.value.metric}`
  const params = new URLSearchParams()

  params.set('op', 'heartbeatRuleSubmit')
  params.set('configs', configs)
  params.set('ruleId', ruleId)
  params.set('metrics', heartbeatMetricJson())
  params.set('available', String(heartbeatRuleForm.value.available))
  params.set('domain', domain)
  params.set('vue', 'true')
  window.location.href = `${contextPath.value}/mvc/s/config?${params.toString()}`
}

function submitAlertDefaultReceivers(mode: string) {
  alertDefaultReceiversMode.value = mode
  requestAnimationFrame(() => {
    const form = document.getElementById('alertDefaultReceiversForm') as HTMLFormElement | null

    form?.requestSubmit()
  })
}

function heartbeatMetricJson() {
  const items = heartbeatMetricItems.value.map((item) => ({
    metricItemText: item.metricItemText,
    productText: item.productText.trim()
  })).filter((item) => item.metricItemText || item.productText)

  return items.length > 0 ? JSON.stringify(items) : ''
}

function alertRuleConfigJson() {
  const configs = alertRuleConfigs.value.map((config) => {
    const conditions = config.conditions.map((condition) => {
      const subConditions = condition.subConditions
        .map((subCondition) => ({
          text: subCondition.text.trim(),
          type: subCondition.type
        }))
        .filter((subCondition) => subCondition.type)
        .map(validateRuleSubCondition)

      if (subConditions.length === 0) {
        return null
      }
      const normalizedCondition: Record<string, unknown> = {
        alertType: condition.alertType,
        'sub-conditions': subConditions
      }
      const minute = condition.minute.trim()

      if (minute) {
        const minuteNumber = Number(minute)

        if (!Number.isFinite(minuteNumber)) {
          throw new Error('持续分钟必须是数字！')
        }
        if (minuteNumber > 10) {
          throw new Error('规则时间请限制在10分钟之内！')
        }
        normalizedCondition.minute = minuteNumber
      }
      return normalizedCondition
    }).filter(Boolean)

    if (conditions.length === 0) {
      return null
    }
    return {
      conditions,
      endtime: config.endtime.trim() || '24:00',
      starttime: config.starttime.trim() || '00:00'
    }
  }).filter(Boolean)

  return configs.length > 0 ? JSON.stringify(configs) : ''
}

function validateRuleSubCondition(subCondition: { text: string, type: string }) {
  if (subCondition.type === 'UserDefine') {
    if (!subCondition.text) {
      throw new Error('自定义监控规则不能为空！')
    }
    return subCondition
  }
  if (!subCondition.text) {
    throw new Error('阈值不能为空！')
  }
  if (!Number.isFinite(Number(subCondition.text))) {
    throw new Error('阈值必须是数字！')
  }
  return subCondition
}

function isJsonResponse(response: Response) {
  return (response.headers.get('content-type') || '').toLowerCase().includes('application/json')
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)

  if (isBusinessConfigAction(currentParams.value.get('op'))) {
    params.set('op', 'vueData')
    params.set('vueAction', businessVueAction(currentParams.value.get('op')))
    if (!params.get('domain')) {
      params.set('domain', currentDomain.value)
    }
    return `${contextPath.value}/mvc/s/business?${params.toString()}`
  }
  params.set('op', 'vueData')
  params.set('vueAction', currentParams.value.get('op') || 'projects')
  if (!params.get('domain')) {
    params.set('domain', currentDomain.value)
  }
  return `${contextPath.value}/mvc/s/config?${params.toString()}`
}

function isBusinessConfigAction(action: string | null) {
  return action === 'businessList' || action === 'businessCustomAdd' || action === 'businessTagConfig'
}

function businessVueAction(action: string | null) {
  if (action === 'businessCustomAdd') {
    return 'customAdd'
  }
  if (action === 'businessTagConfig') {
    return 'tagConfig'
  }
  return 'list'
}

function configUrl(overrides: Record<string, string | undefined>) {
  const params = new URLSearchParams()

  params.set('op', overrides.op || currentParams.value.get('op') || 'projects')
  if (overrides.domain || currentDomain.value) {
    params.set('domain', overrides.domain || currentDomain.value)
  }
  for (const [key, value] of Object.entries(overrides)) {
    if (value && key !== 'op' && key !== 'domain') {
      params.set(key, value)
    }
  }
  return `${contextPath.value}/mvc/vue/s/config?${params.toString()}`
}

function confirmDelete(event: MouseEvent) {
  if (!window.confirm('你确定要删除吗？不可恢复。')) {
    event.preventDefault()
  }
}

function validateProjectAdd(event: SubmitEvent) {
  const form = event.currentTarget as HTMLFormElement
  const formData = new FormData(form)
  const domain = String(formData.get('project.domain') || '').trim()

  if (domain && !isValidProjectDomain(domain)) {
    window.alert('项目名只能包含半角英文、数字、点和中划线。')
    event.preventDefault()
  }
}

function isValidProjectDomain(value: string) {
  return /^[A-Za-z0-9][A-Za-z0-9.-]*$/.test(value)
}

function addDomainGroupRow() {
  editableGroupRows.value.push(toEditableGroupRow({ id: '', ips: [] }, false))
}

function removeDomainGroupRow(index: number) {
  editableGroupRows.value.splice(index, 1)
}

function addPendingIp(index: number) {
  const row = editableGroupRows.value[index]
  const ips = row.pendingIp.split(',').map((item) => item.trim()).filter(Boolean)
  const invalidIp = ips.find((ip) => !isValidIpv4Address(ip))

  if (invalidIp) {
    window.alert(`IP格式不正确：${invalidIp}`)
    return
  }

  for (const ip of ips) {
    if (!row.ips.includes(ip)) {
      row.ips.push(ip)
    }
  }
  row.pendingIp = ''
}

function removeGroupIp(index: number, ip: string) {
  const row = editableGroupRows.value[index]

  row.ips = row.ips.filter((item) => item !== ip)
}

function submitDomainGroup() {
  const domainId = groupDomainInput.value.trim()
  const groups: Record<string, { id: string; ips: string[] }> = {}

  if (!domainId) {
    window.alert('项目组不能为空')
    return
  }
  for (const row of editableGroupRows.value) {
    const id = row.id.trim()

    if (id) {
      const invalidIp = row.ips.find((ip) => !isValidIpv4Address(ip))

      if (invalidIp) {
        window.alert(`IP格式不正确：${invalidIp}`)
        return
      }
      groups[id] = { id, ips: row.ips }
    }
  }

  const content = JSON.stringify({ id: domainId, groups })
  const params = new URLSearchParams()

  params.set('op', 'domainGroupConfigSubmit')
  params.set('domain', domainId)
  params.set('content', content)
  params.set('vue', 'true')
  window.location.href = `${contextPath.value}/mvc/s/config?${params.toString()}`
}

function isValidIpv4Address(value: string) {
  const parts = value.split('.')

  if (parts.length !== 4) {
    return false
  }
  return parts.every((part) => {
    if (!/^\d+$/.test(part)) {
      return false
    }
    if (part.length > 1 && part.startsWith('0')) {
      return false
    }
    const number = Number(part)

    return number >= 0 && number <= 255
  })
}

function legacyConfigUrl(op?: string, overrides: Record<string, string> = {}) {
  const params = new URLSearchParams()

  if (op) {
    params.set('op', op)
  }
  for (const [key, value] of Object.entries(overrides)) {
    params.set(key, value)
  }
  return `${contextPath.value}/mvc/s/config${params.toString() ? `?${params.toString()}` : ''}`
}

function businessConfigUrl(op: string) {
  const params = new URLSearchParams()

  params.set('op', op)
  params.set('domain', currentDomain.value)
  params.set('vue', 'true')
  return `${contextPath.value}/mvc/s/business?${params.toString()}`
}

function businessSubmitUrl(op: string) {
  const params = new URLSearchParams()

  params.set('op', op)
  params.set('domain', currentDomain.value)
  return `${contextPath.value}/mvc/s/business?${params.toString()}`
}

function configSubmitUrl(op: string, overrides: Record<string, string | undefined> = {}) {
  const params = new URLSearchParams()

  params.set('op', op)
  params.set('domain', currentDomain.value)
  for (const [key, value] of Object.entries(overrides)) {
    if (value) {
      params.set(key, value)
    }
  }
  return `${contextPath.value}/mvc/s/config?${params.toString()}`
}

function routerBuildUrl() {
  return `${contextPath.value}/mvc/s/router?op=build`
}

function confirmRouterBuild(event: MouseEvent) {
  if (!window.confirm('确认重算路由？')) {
    event.preventDefault()
  }
}

function alertRuleUpdateUrl(ruleId?: string) {
  return configUrl({ op: alertRuleUpdateAction.value, domain: currentDomain.value, ruleId })
}

function alertRuleDeleteUrl(ruleId: string) {
  return legacyConfigUrl(alertRuleDeleteAction.value, { ruleId, vue: 'true' })
}

function exceptionThresholdDeleteUrl(item: ExceptionLimit) {
  return legacyConfigUrl('exceptionThresholdDelete', {
    domain: item.domain,
    exception: item.name,
    type: 'threshold',
    vue: 'true'
  })
}

function exceptionExcludeDeleteUrl(item: ExceptionExclude) {
  return legacyConfigUrl('exceptionExcludeDelete', {
    domain: item.domain,
    exception: item.name,
    type: 'exclude',
    vue: 'true'
  })
}

function heartbeatRuleDeleteUrl(ruleId: string) {
  return legacyConfigUrl('heartbeatRulDelete', { key: ruleId, vue: 'true' })
}

function monitorLabel(monitor: string) {
  const labels: Record<string, string> = {
    avg: '响应时间',
    count: '执行次数',
    failRatio: '失败率',
    max: '最大响应时间'
  }

  return labels[monitor] || monitor
}

function isSuccessState(value: boolean | string | null | undefined) {
  return value === true || value === 'true' || value === 'Success'
}

function isFailureState(value: boolean | string | null | undefined) {
  return value === false || value === 'false' || value === 'Fail' || value === 'Failure'
}

function searchDomains(query: string, callback: (items: Array<{ category: string; label: string; value: string }>) => void) {
  const keyword = query.trim().toLowerCase()

  if (!keyword) {
    callback(projectSuggestions.value)
    return
  }
  callback(projectSuggestions.value.filter((item) => item.value.toLowerCase().includes(keyword)))
}

function searchProject() {
  window.location.href = configUrl({ domain: domainInput.value || 'cat', op: 'projects' })
}

function searchBusinessDomains(query: string, callback: (items: Array<{ value: string }>) => void) {
  const keyword = query.trim().toLowerCase()
  const domains = report.value?.domains || []
  const matched = keyword ? domains.filter((item) => item.toLowerCase().includes(keyword)) : domains

  callback(matched.map((item) => ({ value: item })))
}

function queryBusinessDomain() {
  window.location.href = configUrl({ domain: businessDomainInput.value || 'cat', op: 'businessList' })
}

function selectBusinessDomain(item: { value: string }) {
  businessDomainInput.value = item.value
  queryBusinessDomain()
}

function businessAlertUrl(key: string, attributes: string) {
  const params = new URLSearchParams()

  params.set('op', 'alertRuleAdd')
  params.set('key', key)
  params.set('domain', currentDomain.value)
  params.set('attributes', attributes)
  params.set('vue', 'true')
  return `${contextPath.value}/mvc/s/business?${params.toString()}`
}

function businessEditUrl(item: BusinessConfigItem) {
  if (item.custom) {
    return configUrl({ op: 'businessCustomAdd', domain: currentDomain.value, key: item.id })
  }
  const params = new URLSearchParams()

  params.set('op', 'add')
  params.set('key', item.id)
  params.set('domain', currentDomain.value)
  params.set('vue', 'true')
  return `${contextPath.value}/mvc/s/business?${params.toString()}`
}

function businessDeleteUrl(item: BusinessConfigItem) {
  const params = new URLSearchParams()

  params.set('op', item.custom ? 'customDelete' : 'delete')
  params.set('key', item.id)
  params.set('domain', currentDomain.value)
  params.set('vue', 'true')
  return `${contextPath.value}/mvc/s/business?${params.toString()}`
}

function selectDomain(item: { value: string }) {
  domainInput.value = item.value
  searchProject()
}
</script>
