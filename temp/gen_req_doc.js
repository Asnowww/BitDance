const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, LevelFormat, HeadingLevel,
  BorderStyle, WidthType, ShadingType, PageNumber, PageBreak,
  TableOfContents
} = require("docx");

const border = { style: BorderStyle.SINGLE, size: 1, color: "AAAAAA" };
const borders = { top: border, bottom: border, left: border, right: border };
const cellMargins = { top: 60, bottom: 60, left: 100, right: 100 };

// Helper: create a table cell
function cell(text, opts = {}) {
  const runs = Array.isArray(text) ? text : [new TextRun({ text, font: "Microsoft YaHei", size: 21, ...opts.runOpts })];
  return new TableCell({
    borders,
    margins: cellMargins,
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
    shading: opts.shading ? { fill: opts.shading, type: ShadingType.CLEAR } : undefined,
    children: [new Paragraph({ alignment: opts.align || AlignmentType.LEFT, children: runs })],
    verticalMerge: opts.vmerge,
    columnSpan: opts.colspan,
  });
}

function headerCell(text, width) {
  return cell(text, { width, shading: "2E75B6", runOpts: { bold: true, color: "FFFFFF", size: 21 } });
}

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 360, after: 200 },
    children: [new TextRun({ text, font: "Microsoft YaHei", size: 32, bold: true, color: "1F4E79" })],
  });
}
function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 280, after: 160 },
    children: [new TextRun({ text, font: "Microsoft YaHei", size: 28, bold: true, color: "2E75B6" })],
  });
}
function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 200, after: 120 },
    children: [new TextRun({ text, font: "Microsoft YaHei", size: 24, bold: true, color: "2E75B6" })],
  });
}
function p(text, opts = {}) {
  return new Paragraph({
    spacing: { after: 120, line: 360 },
    alignment: opts.align || AlignmentType.LEFT,
    children: [new TextRun({ text, font: "Microsoft YaHei", size: 21, ...opts })],
  });
}
function pb(label, value) {
  return new Paragraph({
    spacing: { after: 80, line: 360 },
    children: [
      new TextRun({ text: label, font: "Microsoft YaHei", size: 21, bold: true }),
      new TextRun({ text: value, font: "Microsoft YaHei", size: 21 }),
    ],
  });
}

const numbering = {
  config: [
    {
      reference: "bullets",
      levels: [{
        level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT,
        style: { paragraph: { indent: { left: 720, hanging: 360 } } }
      }]
    },
    {
      reference: "bullets2",
      levels: [{
        level: 0, format: LevelFormat.BULLET, text: "\u25CB", alignment: AlignmentType.LEFT,
        style: { paragraph: { indent: { left: 1080, hanging: 360 } } }
      }]
    },
  ]
};

function bullet(text, ref = "bullets") {
  return new Paragraph({
    numbering: { reference: ref, level: 0 },
    spacing: { after: 60, line: 340 },
    children: [new TextRun({ text, font: "Microsoft YaHei", size: 21 })],
  });
}

// ========== 用例表格 ==========
function useCaseTable(id, name, actor, precondition, steps, postcondition, exceptions) {
  const TW = 9072;
  const c1w = 1800;
  const c2w = TW - c1w;
  const rows = [
    new TableRow({ children: [headerCell("用例编号", c1w), cell(id, { width: c2w })] }),
    new TableRow({ children: [headerCell("用例名称", c1w), cell(name, { width: c2w })] }),
    new TableRow({ children: [headerCell("参与者", c1w), cell(actor, { width: c2w })] }),
    new TableRow({ children: [headerCell("前置条件", c1w), cell(precondition, { width: c2w })] }),
    new TableRow({
      children: [
        headerCell("基本流程", c1w),
        cell(steps.map((s, i) => `${i + 1}. ${s}`).join("\n"), { width: c2w }),
      ]
    }),
    new TableRow({ children: [headerCell("后置条件", c1w), cell(postcondition, { width: c2w })] }),
    new TableRow({ children: [headerCell("异常流程", c1w), cell(exceptions || "无", { width: c2w })] }),
  ];
  return new Table({
    width: { size: TW, type: WidthType.DXA },
    columnWidths: [c1w, c2w],
    rows,
  });
}

// ========== 功能需求表格 ==========
function funcTable(headers, data) {
  const TW = 9072;
  const widths = headers.map((_, i) => {
    if (headers.length === 4) return [1200, 2000, 4072, 1800][i];
    if (headers.length === 3) return [1500, 3072, 4500][i];
    return Math.floor(TW / headers.length);
  });
  const hRow = new TableRow({
    children: headers.map((h, i) => headerCell(h, widths[i]))
  });
  const dRows = data.map(row =>
    new TableRow({
      children: row.map((d, i) => cell(d, { width: widths[i] }))
    })
  );
  return new Table({
    width: { size: TW, type: WidthType.DXA },
    columnWidths: widths,
    rows: [hRow, ...dRows],
  });
}

// ========== NFR table ==========
function nfrTable(data) {
  const TW = 9072;
  const widths = [1500, 4572, 3000];
  const hRow = new TableRow({
    children: ["类别", "需求描述", "指标/标准"].map((h, i) => headerCell(h, widths[i]))
  });
  const dRows = data.map(row =>
    new TableRow({ children: row.map((d, i) => cell(d, { width: widths[i] })) })
  );
  return new Table({ width: { size: TW, type: WidthType.DXA }, columnWidths: widths, rows: [hRow, ...dRows] });
}

// ========== DOCUMENT ==========
const children = [];

// Title page
children.push(
  new Paragraph({ spacing: { before: 3000 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "BitDance", font: "Microsoft YaHei", size: 52, bold: true, color: "2E75B6" })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
    children: [new TextRun({ text: "舞蹈学习与约练平台", font: "Microsoft YaHei", size: 40, bold: true, color: "1F4E79" })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 600 },
    children: [new TextRun({ text: "产品需求分析与功能设计文档", font: "Microsoft YaHei", size: 36, color: "404040" })],
  }),
  new Paragraph({ spacing: { before: 800 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER, spacing: { after: 100 },
    children: [new TextRun({ text: "第五组", font: "Microsoft YaHei", size: 24, color: "666666" })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER, spacing: { after: 100 },
    children: [new TextRun({ text: "版本：V1.0", font: "Microsoft YaHei", size: 24, color: "666666" })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER, spacing: { after: 100 },
    children: [new TextRun({ text: "日期：2026年4月7日", font: "Microsoft YaHei", size: 24, color: "666666" })],
  }),
  new Paragraph({ children: [new PageBreak()] }),
);

// TOC
children.push(
  h1("目录"),
  new TableOfContents("目录", { hyperlink: true, headingStyleRange: "1-3" }),
  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第一章 文档说明 ====================
children.push(
  h1("一、文档说明"),
  h2("1.1 文档目的"),
  p("本文档是《BitDance舞蹈学习与约练平台》的产品需求分析与功能设计文档，基于已通过的开题立项书编写。本文档旨在对产品进行系统的需求分析，明确用户画像与核心场景，定义各功能模块的详细设计，为后续的技术架构设计、UI/UX设计和开发实施提供完整的产品层面依据。"),
  h2("1.2 文档范围"),
  bullet("产品整体需求分析：用户画像、需求层次、场景分析"),
  bullet("功能需求规格：各模块功能清单、用例描述、业务规则"),
  bullet("非功能需求：性能、安全、兼容性等"),
  bullet("信息架构与页面流程设计"),
  bullet("数据实体关系设计"),
  bullet("MVP与迭代规划"),
  h2("1.3 术语定义"),
  funcTable(["术语", "说明", "备注"], [
    ["BitDance", "本项目产品名称，舞蹈学习与约练平台", ""],
    ["舞室", "提供舞蹈课程教学的线下培训场所/工作室", ""],
    ["约练", "用户之间约定时间地点一起练习舞蹈", ""],
    ["搭子", "一起学舞、练舞的同伴", ""],
    ["Workshop", "由知名舞者/老师举办的短期授课活动", "通常1-3天"],
    ["成长档案", "记录用户学舞历程和进步轨迹的个人数据", ""],
    ["拼课", "多个用户合拼凑够人数一起上私教/小班课", ""],
  ]),
  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第二章 产品需求分析 ====================
children.push(
  h1("二、产品需求分析"),

  h2("2.1 用户画像分析"),
  h3("2.1.1 核心用户画像A：舞蹈入门新手"),
  pb("年龄范围：", "18-28岁，以大学生和初入职场的年轻人为主"),
  pb("典型特征：", "对舞蹈感兴趣但尚未开始或刚开始学习，信息获取依赖短视频种草和朋友推荐"),
  pb("核心痛点：", ""),
  bullet("不知道该选哪家舞室、哪个舞种、哪位老师"),
  bullet("担心零基础跟不上课程节奏"),
  bullet("缺少同伴，一个人报名容易放弃"),
  bullet("在多个平台间反复搜索比较，决策成本高"),
  pb("核心诉求：", "快速找到适合零基础的舞室和课程，获得真实可信的评价参考，最好能找到一起学的搭子"),

  h3("2.1.2 核心用户画像B：持续学舞的进阶用户"),
  pb("年龄范围：", "18-35岁，有半年以上学舞经验"),
  pb("典型特征：", "已有固定学习的舞种，关注特定老师和Workshop，有练舞习惯"),
  pb("核心痛点：", ""),
  bullet("想找到水平相当的练舞搭子，但线下组织效率低"),
  bullet("想参加Workshop但信息分散，容易错过报名"),
  bullet("缺少系统化的成长记录和进步可视化"),
  bullet("想尝试新舞种或新舞室，但缺乏针对性参考"),
  pb("核心诉求：", "高效约练、Workshop信息聚合、成长记录沉淀、跨舞室课程发现"),

  h3("2.1.3 商家用户画像C：舞室经营者/独立教练"),
  pb("典型特征：", "经营舞蹈工作室或以个人身份授课的舞蹈教练"),
  pb("核心痛点：", ""),
  bullet("获客依赖美团和私域，缺少精准触达舞蹈学习者的渠道"),
  bullet("课程信息展示受限于泛平台模板，无法突出教学特色"),
  bullet("缺少对学员画像的了解，难以优化课程设置"),
  pb("核心诉求：", "精准获客、专业化课程展示、学员管理、品牌建设"),

  new Paragraph({ children: [new PageBreak()] }),

  h2("2.2 需求层次分析"),
  p("基于用户画像和立项书中的业务路径分析，将产品需求按优先级划分为以下层次："),
  funcTable(["优先级", "需求层次", "说明", "对应模块"], [
    ["P0 必须", "舞室发现与搜索", "用户能按位置、舞种、价格等条件快速找到舞室", "舞室搜索模块"],
    ["P0 必须", "课程信息展示", "结构化展示课程详情，支持多维度对比", "课程详情模块"],
    ["P0 必须", "结构化评价", "围绕舞蹈学习场景的专业评价体系", "评价系统模块"],
    ["P0 必须", "用户账号体系", "注册、登录、个人资料管理", "账号模块"],
    ["P1 重要", "约搭子/约练", "基于舞种、位置、时间匹配练舞同伴", "约练社交模块"],
    ["P1 重要", "成长记录与打卡", "学舞历程记录、训练打卡、数据统计", "成长档案模块"],
    ["P1 重要", "收藏与关注", "收藏舞室/课程/老师，关注感兴趣的内容", "个人中心模块"],
    ["P2 期望", "内容社区", "分享试听感受、课堂记录、约练日常", "社区模块"],
    ["P2 期望", "Workshop管理", "Workshop发布、报名、候补、签到", "活动模块"],
    ["P2 期望", "商家入驻与管理", "舞室认领、课表维护、数据看板", "商家后台模块"],
    ["P3 锦上添花", "智能推荐", "基于用户偏好推荐舞室、课程、搭子", "推荐系统"],
    ["P3 锦上添花", "拼课功能", "用户发起拼课需求，凑齐人数开课", "拼课模块"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),

  h2("2.3 核心用户场景分析"),
  h3("场景一：新手找舞室决策"),
  pb("用户：", "小李，大二学生，想学韩舞，零基础"),
  pb("场景描述：", "小李在短视频上刷到韩舞翻跳视频后想学舞，但不知道学校附近哪家舞室有韩舞课、价格多少、是否适合零基础。"),
  pb("当前痛点：", "需要在美团搜位置、抖音看风格、小红书看评价、微信群问口碑，来回切换4个以上平台。"),
  pb("期望体验：", "打开BitDance，定位到学校附近，筛选\"韩舞+零基础友好\"，看到3家舞室的课程对比，阅读结构化评价后收藏心仪课程并预约试听。"),

  h3("场景二：进阶用户约练"),
  pb("用户：", "小王，上班族，学Hiphop一年，想找人周末一起练舞"),
  pb("场景描述：", "小王工作日上课、周末想找人一起复习和练习，但舞室的微信群里不好意思频繁发约练信息。"),
  pb("当前痛点：", "在群里发约练信息响应率低，且不知道对方水平是否匹配。"),
  pb("期望体验：", "在BitDance发布约练信息（Hiphop/中级/周六下午/某舞室），平台推荐匹配的舞友，双方确认后即可约练。"),

  h3("场景三：Workshop报名"),
  pb("用户：", "小张，Urban舞者，关注某位抖音舞蹈博主"),
  pb("场景描述：", "某知名老师要来本城市开Workshop，小张想第一时间报名。"),
  pb("当前痛点：", "Workshop信息散落在不同舞室的公众号和朋友圈，经常错过报名时间，且不确定场地和时间。"),
  pb("期望体验：", "在BitDance收到Workshop上线通知，查看详情（时间、地点、价格、老师介绍、往期评价），一键报名并完成支付。"),

  h3("场景四：舞室经营者获客"),
  pb("用户：", "陈老师，经营一家主打Jazz和韩舞的小型舞室"),
  pb("场景描述：", "舞室开业半年，想吸引更多零基础学员。"),
  pb("当前痛点：", "美团上同类商家太多且竞价激烈，短视频运营成本高，获客效率低。"),
  pb("期望体验：", "在BitDance认领舞室页面，完善课程信息和教练介绍，通过平台的精准匹配触达真正想学韩舞的新手用户。"),

  new Paragraph({ children: [new PageBreak()] }),

  h2("2.4 需求分析总结"),
  p("通过对用户画像、需求层次和核心场景的分析，BitDance平台的产品需求可以归纳为以下关键结论："),
  bullet("决策支持是第一优先级：用户最迫切的需求是在一个平台上完成舞室发现、课程对比和可信评价的闭环，这是平台的核心价值锚点。"),
  bullet("学习陪伴是留存关键：约搭子、约练和成长记录解决的是\"报名之后怎么坚持\"的问题，直接决定用户生命周期。"),
  bullet("内容与活动是增长飞轮：社区内容和Workshop活动既能带来新用户，也能提升老用户活跃度，但不应喧宾夺主。"),
  bullet("商家服务是商业化基础：舞室和教练端的入驻与管理能力是未来变现的核心，但前期以轻量接入为主。"),
);

// ==================== 第三章 功能设计 ====================
children.push(
  new Paragraph({ children: [new PageBreak()] }),
  h1("三、功能设计"),

  h2("3.1 功能架构总览"),
  p("BitDance平台的功能体系围绕\"搜索决策 → 学习陪伴 → 成长沉淀\"的用户主路径展开，分为以下六大功能模块："),
  funcTable(["模块编号", "模块名称", "核心职责", "优先级"], [
    ["M1", "舞室与课程模块", "舞室搜索、筛选、详情展示、课程信息管理", "P0"],
    ["M2", "评价系统模块", "结构化评价、评分体系、评价风控", "P0"],
    ["M3", "用户账号模块", "注册登录、个人资料、偏好设置", "P0"],
    ["M4", "约练社交模块", "约搭子、约练发布、匹配推荐", "P1"],
    ["M5", "成长档案模块", "训练打卡、学习记录、成长数据可视化", "P1"],
    ["M6", "社区与活动模块", "内容分享、Workshop管理、活动发布", "P2"],
    ["M7", "商家管理模块", "舞室入驻、课表维护、教练管理、数据看板", "P2"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M1 舞室与课程模块 =====
  h2("3.2 M1 舞室与课程模块"),
  h3("3.2.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M1-F01", "附近舞室搜索", "基于用户定位展示附近舞室列表，支持地图模式和列表模式切换", "P0"],
    ["M1-F02", "多维度筛选", "支持按舞种、价格区间、距离、课程时段、适合人群、课程强度筛选", "P0"],
    ["M1-F03", "舞室详情页", "展示舞室基础信息、环境照片、主打舞种、课表、地理位置及导航", "P0"],
    ["M1-F04", "课程详情页", "展示课程名称、舞种、难度、价格、老师、适合人群、课程目标、训练强度", "P0"],
    ["M1-F05", "老师详情页", "展示老师擅长舞种、教学风格、课堂评价、可预约课程", "P0"],
    ["M1-F06", "收藏功能", "用户可收藏舞室、课程、老师，收藏列表在个人中心管理", "P1"],
    ["M1-F07", "课表查看", "按日/周视图展示舞室课程安排，支持筛选舞种和老师", "P1"],
    ["M1-F08", "试听预约", "用户可在线预约试听课程，舞室收到预约通知", "P1"],
    ["M1-F09", "舞室对比", "支持选择2-3家舞室进行价格、舞种、评分等维度的并排对比", "P2"],
    ["M1-F10", "智能推荐", "根据用户偏好（舞种、位置、预算）推荐匹配舞室和课程", "P3"],
  ]),

  h3("3.2.2 核心用例"),
  p("用例UC-01：用户搜索附近舞室"),
  useCaseTable(
    "UC-01", "搜索附近舞室", "普通用户",
    "用户已登录或以游客身份进入平台，已授权地理位置",
    [
      "用户进入首页，系统获取用户当前位置",
      "系统展示附近舞室列表（默认按距离排序）",
      "用户可切换为地图模式查看舞室分布",
      "用户点击筛选按钮，设置筛选条件（如：韩舞、5km内、200元以下）",
      "系统根据筛选条件刷新舞室列表",
      "用户点击某舞室卡片，进入舞室详情页",
    ],
    "用户查看到符合条件的舞室列表",
    "a. 用户未授权定位：提示手动输入地址或选择城市\nb. 附近无符合条件的舞室：提示扩大搜索范围或调整筛选条件"
  ),

  p(""),
  p("用例UC-02：用户查看课程详情并收藏"),
  useCaseTable(
    "UC-02", "查看课程详情并收藏", "普通用户",
    "用户已进入某舞室详情页",
    [
      "用户在舞室详情页浏览课程列表",
      "用户点击某门课程，进入课程详情页",
      "系统展示课程信息：名称、舞种、难度等级、适合人群、老师信息、课程目标、训练强度、价格、上课时间",
      "系统展示该课程的结构化评价摘要（综合评分、各维度评分）",
      "用户点击收藏按钮，系统将课程加入用户收藏列表",
      "用户可点击\"预约试听\"发起试听申请",
    ],
    "课程被加入收藏列表，或试听预约已提交",
    "a. 课程已下架：提示课程不可用\nb. 用户未登录点击收藏：引导登录"
  ),

  h3("3.2.3 业务规则"),
  bullet("舞室搜索默认范围为用户当前位置5km内，最大可扩展至50km"),
  bullet("舞种分类采用二级分类：一级为大类（街舞、流行舞、中国舞等），二级为具体舞种（Hiphop、Jazz、韩舞、Urban、Locking、Breaking等）"),
  bullet("价格筛选支持区间选择：0-50元/节、50-100元/节、100-200元/节、200元以上/节"),
  bullet("课程难度分为四级：零基础友好、初级、中级、高级"),
  bullet("适合人群标签包括：零基础、有基础、少儿、成人、上班族等"),
  bullet("舞室信息来源分三层：平台初始录入、商家认领完善、用户纠错补充"),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M2 评价系统模块 =====
  h2("3.3 M2 评价系统模块"),
  h3("3.3.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M2-F01", "舞室评价", "用户对舞室进行多维度结构化评价（交通、卫生、场地、氛围）", "P0"],
    ["M2-F02", "老师评价", "用户对老师进行结构化评价（耐心度、纠错质量、讲解清晰度、零基础照顾）", "P0"],
    ["M2-F03", "课程评价", "用户对课程进行结构化评价（上手难度、节奏快慢、练习强度、实际收获）", "P0"],
    ["M2-F04", "评价权重分层", "根据用户是否通过平台报名/签到/核销区分评价权重", "P0"],
    ["M2-F05", "评分聚合展示", "将结构化评分聚合为综合评分和各维度评分雷达图", "P1"],
    ["M2-F06", "图文/视频评价", "支持用户在评价中附加照片或短视频", "P1"],
    ["M2-F07", "评价风控", "识别异常评价（水军、刷评、控评），进行降权/折叠/人工复核", "P1"],
    ["M2-F08", "商家申诉", "舞室可对不实评价发起申诉，进入人工审核流程", "P2"],
  ]),

  h3("3.3.2 评价维度设计"),
  p("评价体系按三个对象分别设计评价维度，每个维度采用1-5分评分制："),

  p("舞室评价维度：", { bold: true }),
  funcTable(["维度", "说明", "评分标准"], [
    ["交通便利度", "舞室到达是否方便", "1=很不方便 5=非常方便"],
    ["环境卫生", "舞室整洁程度", "1=很差 5=非常好"],
    ["场地条件", "镜面、地板、音响等硬件条件", "1=简陋 5=专业"],
    ["整体氛围", "舞室的学习氛围和社交氛围", "1=冷清 5=活跃友好"],
  ]),

  p("老师评价维度：", { bold: true }),
  funcTable(["维度", "说明", "评分标准"], [
    ["耐心程度", "对学员尤其是新手的耐心", "1=不耐烦 5=非常耐心"],
    ["纠错质量", "能否准确发现并纠正动作问题", "1=很少纠错 5=细致纠错"],
    ["讲解清晰度", "动作拆解和教学讲解是否清楚", "1=难以理解 5=非常清楚"],
    ["零基础友好", "是否照顾零基础学员", "1=完全不照顾 5=非常照顾"],
  ]),

  p("课程评价维度：", { bold: true }),
  funcTable(["维度", "说明", "评分标准"], [
    ["上手难度", "课程对学员能力的要求", "1=很难跟上 5=轻松上手"],
    ["节奏合理性", "教学进度是否适中", "1=太快/太慢 5=节奏刚好"],
    ["练习强度", "课程的体力消耗程度", "1=过于轻松 5=强度很大"],
    ["实际收获", "课程结束后的能力提升感受", "1=没有收获 5=收获很大"],
  ]),

  h3("3.3.3 评价权重与风控规则"),
  bullet("高权重评价：通过平台完成报名、签到或核销的用户，其评价标记为\"已验证\"，权重最高"),
  bullet("普通权重评价：注册用户自主提交的评价，需经过基础审核后展示"),
  bullet("低权重/折叠评价：被风控系统标记的异常评价，包括——"),
  bullet("  - 新注册账号（<7天）发布的评价"),
  bullet("  - 短时间内同一舞室出现大量同质化好评/差评"),
  bullet("  - 文案高度相似的批量评价"),
  bullet("  - 单店评分出现统计学异常波动"),
  bullet("舞室不能直接删除评价，只能通过申诉渠道提交审核"),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M3 用户账号模块 =====
  h2("3.4 M3 用户账号模块"),
  h3("3.4.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M3-F01", "手机号注册/登录", "通过手机号+验证码完成注册和登录", "P0"],
    ["M3-F02", "第三方登录", "支持微信登录快速授权", "P0"],
    ["M3-F03", "个人资料管理", "编辑昵称、头像、性别、个人简介", "P0"],
    ["M3-F04", "舞蹈偏好设置", "设置感兴趣的舞种、学习水平、学习目标", "P1"],
    ["M3-F05", "社交账号展示", "用户可在个人主页展示其他社交平台账号", "P2"],
    ["M3-F06", "隐私设置", "控制个人信息、学习记录、约练状态的可见性", "P1"],
    ["M3-F07", "消息通知", "系统通知、约练回复、评价互动等消息推送", "P1"],
  ]),

  h3("3.4.2 用户角色设计"),
  funcTable(["角色", "说明", "权限范围"], [
    ["游客", "未注册或未登录的用户", "浏览舞室/课程信息，不可评价、收藏、约练"],
    ["普通用户", "已注册登录的个人用户", "全部C端功能：搜索、评价、收藏、约练、打卡、社区"],
    ["舞室管理员", "认领舞室的经营者", "管理舞室信息、课表、回复评价、查看数据看板"],
    ["教练", "在舞室授课的老师", "管理个人主页、查看个人评价、发布可约课程"],
    ["平台管理员", "平台运营人员", "内容审核、评价风控、数据管理、商家审核"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M4 约练社交模块 =====
  h2("3.5 M4 约练社交模块"),
  h3("3.5.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M4-F01", "发布约练", "用户发布约练信息：舞种、时间、地点、人数、水平要求", "P1"],
    ["M4-F02", "约练广场", "展示附近/同城的约练信息列表，支持按舞种、时间、距离筛选", "P1"],
    ["M4-F03", "约练匹配推荐", "根据用户舞种、水平、位置偏好推荐合适的约练和搭子", "P2"],
    ["M4-F04", "约练响应", "用户可响应他人的约练邀请，双方确认后建立约练关系", "P1"],
    ["M4-F05", "搭子关系", "互相约练过的用户可互加为搭子，方便后续直接邀约", "P2"],
    ["M4-F06", "约练评价", "约练结束后双方可进行简单评价（守时、友好、水平匹配）", "P2"],
    ["M4-F07", "拼课发起", "用户发起拼课需求，凑齐人数后联系舞室开课", "P3"],
  ]),

  h3("3.5.2 核心用例"),
  p("用例UC-03：用户发布约练并匹配搭子"),
  useCaseTable(
    "UC-03", "发布约练并匹配搭子", "普通用户",
    "用户已登录，已设置舞蹈偏好",
    [
      "用户进入约练广场，点击\"发布约练\"",
      "用户填写约练信息：舞种（Hiphop）、时间（周六14:00-16:00）、地点（某舞室/指定场地）、期望人数（2-4人）、水平要求（中级）",
      "系统发布约练信息至约练广场",
      "系统同时向符合条件的用户推送约练通知",
      "其他用户浏览到该约练信息，点击\"我要参加\"",
      "发起者收到响应通知，确认接受",
      "双方约练关系建立，系统发送约练详情提醒",
    ],
    "约练成功建立，双方收到时间地点提醒",
    "a. 无人响应：约练到期自动关闭\nb. 发起者取消：通知已响应用户\nc. 人数已满：后续用户显示\"已满员\""
  ),

  h3("3.5.3 业务规则"),
  bullet("约练信息有效期默认为发布后7天，到期自动关闭"),
  bullet("每人同时最多发布3条约练信息，防止刷屏"),
  bullet("约练确认后取消需提前2小时，频繁取消者将被限制发布权限"),
  bullet("约练完成后系统自动触发双向评价邀请"),
  bullet("搭子关系基于双方约练完成后的互相添加，非单方面关注"),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M5 成长档案模块 =====
  h2("3.6 M5 成长档案模块"),
  h3("3.6.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M5-F01", "训练打卡", "用户每次练舞后进行打卡，记录舞种、时长、地点", "P1"],
    ["M5-F02", "学习数据统计", "统计学舞天数、累计训练时长、已上课程数、尝试舞种数", "P1"],
    ["M5-F03", "成长时间线", "以时间线形式展示用户的学舞历程和关键节点", "P1"],
    ["M5-F04", "阶段作品记录", "用户上传练舞视频/照片作为阶段性成果存档", "P2"],
    ["M5-F05", "学习目标设置", "设置周/月训练目标（如：本周练舞3次），系统追踪完成度", "P2"],
    ["M5-F06", "成长报告", "按月/季度生成学习成长报告，包含数据概览和进步对比", "P3"],
    ["M5-F07", "成就徽章", "达成特定里程碑解锁徽章（如：连续打卡30天、尝试5种舞种）", "P3"],
  ]),

  h3("3.6.2 核心用例"),
  p("用例UC-04：用户进行训练打卡"),
  useCaseTable(
    "UC-04", "训练打卡", "普通用户",
    "用户已登录",
    [
      "用户完成练舞后，进入\"打卡\"页面",
      "系统自动填充当前日期和时间",
      "用户选择本次练习的舞种、填写练习时长",
      "用户可选填练习地点、练习内容、心得感受",
      "用户可选择上传练习视频/照片",
      "点击\"完成打卡\"，系统记录打卡数据",
      "系统更新用户的学习数据统计和成长时间线",
    ],
    "打卡记录保存成功，用户数据统计更新",
    "a. 当日已打卡：提示是否追加记录"
  ),

  h3("3.6.3 成长数据指标"),
  funcTable(["数据指标", "说明", "展示形式"], [
    ["累计学舞天数", "从首次打卡到当前的练舞天数", "数字+日历热力图"],
    ["总训练时长", "累计练舞小时数", "数字+趋势折线图"],
    ["已上课程数", "通过平台记录的课程数量", "数字"],
    ["尝试舞种数", "练习过的不同舞种数量", "数字+舞种标签"],
    ["连续打卡天数", "当前最长连续打卡记录", "数字+进度条"],
    ["本周/本月训练量", "当前周期内的训练时长和次数", "柱状图"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M6 社区与活动模块 =====
  h2("3.7 M6 社区与活动模块"),
  h3("3.7.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M6-F01", "动态发布", "用户发布图文/视频动态，分享试听感受、课堂记录、约练日常", "P2"],
    ["M6-F02", "动态互动", "点赞、评论、转发动态", "P2"],
    ["M6-F03", "话题标签", "支持话题分类（#韩舞推荐 #零基础日记 #约练日常）", "P2"],
    ["M6-F04", "Workshop发布", "舞室/主办方发布Workshop信息", "P2"],
    ["M6-F05", "Workshop报名", "用户在线报名Workshop，支持候补排队", "P2"],
    ["M6-F06", "Workshop签到", "到场后扫码签到，关联评价权重", "P2"],
    ["M6-F07", "活动日历", "聚合展示近期本地舞蹈活动和Workshop", "P2"],
    ["M6-F08", "内容审核", "UGC内容的自动审核和人工复核机制", "P2"],
  ]),

  h3("3.7.2 Workshop业务流程"),
  p("Workshop从发布到结束的完整流程如下："),
  bullet("1. 舞室/主办方在商家后台发布Workshop信息（老师、时间、地点、价格、人数上限、报名截止时间）"),
  bullet("2. 平台审核通过后，Workshop信息上线，向关注相关舞种/老师的用户推送通知"),
  bullet("3. 用户浏览Workshop详情，查看老师介绍、往期评价、场地信息"),
  bullet("4. 用户点击报名，选择场次，完成支付"),
  bullet("5. 报名满员后，后续用户进入候补队列"),
  bullet("6. 有用户取消时，系统自动通知候补用户递补"),
  bullet("7. 活动当天，用户到场扫码签到"),
  bullet("8. 活动结束后，系统邀请已签到用户进行评价"),

  new Paragraph({ children: [new PageBreak()] }),

  // ===== M7 商家管理模块 =====
  h2("3.8 M7 商家管理模块"),
  h3("3.8.1 功能清单"),
  funcTable(["功能编号", "功能名称", "功能描述", "优先级"], [
    ["M7-F01", "舞室认领", "舞室经营者认领平台上的舞室页面，提交营业执照等资质审核", "P2"],
    ["M7-F02", "舞室信息管理", "编辑舞室介绍、环境照片、营业时间、联系方式", "P2"],
    ["M7-F03", "课表管理", "创建/编辑/删除课程，设置课表安排", "P2"],
    ["M7-F04", "教练管理", "添加/管理舞室教练账号，区分全职/签约/外聘", "P2"],
    ["M7-F05", "预约管理", "查看和处理用户的试听预约", "P2"],
    ["M7-F06", "评价管理", "查看用户评价，对不实评价发起申诉", "P2"],
    ["M7-F07", "数据看板", "查看舞室曝光量、收藏量、预约量、评分趋势等数据", "P2"],
    ["M7-F08", "Workshop管理", "发布和管理Workshop活动，查看报名和签到数据", "P2"],
  ]),

  h3("3.8.2 角色权限矩阵"),
  p("商家端的角色与权限设计如下："),
  funcTable(["功能", "舞室管理员", "全职教练", "签约教练", "自由教练"], [
    ["编辑舞室信息", "✓", "✗", "✗", "✗"],
    ["管理课表", "✓", "仅自己课程", "仅自己课程", "✗"],
    ["发布Workshop", "✓", "需审批", "需审批", "独立发布"],
    ["查看舞室数据", "✓", "部分", "✗", "✗"],
    ["管理教练账号", "✓", "✗", "✗", "✗"],
    ["回复用户评价", "✓", "仅自己评价", "仅自己评价", "仅自己评价"],
    ["课程收益归属", "舞室", "舞室", "按协议", "教练本人"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第四章 信息架构 ====================
children.push(
  h1("四、信息架构与页面流程"),

  h2("4.1 整体信息架构"),
  p("平台的页面层级结构如下："),

  p("首页（Tab 1）", { bold: true }),
  bullet("附近舞室推荐"),
  bullet("搜索入口（舞种/舞室名称/区域）"),
  bullet("热门舞种快捷入口"),
  bullet("精选课程推荐"),
  bullet("近期Workshop推荐"),

  p("搜索结果页", { bold: true }),
  bullet("舞室列表视图 / 地图视图切换"),
  bullet("筛选面板（舞种、距离、价格、难度、时段）"),
  bullet("排序方式（距离、评分、价格）"),

  p("舞室详情页", { bold: true }),
  bullet("基础信息区（名称、地址、营业时间、联系方式）"),
  bullet("环境展示区（轮播图）"),
  bullet("课程列表区（按舞种分组）"),
  bullet("老师列表区"),
  bullet("评价汇总区（综合评分+维度雷达图+评价列表）"),
  bullet("操作区（收藏、分享、导航、预约试听）"),

  p("约练广场（Tab 2）", { bold: true }),
  bullet("约练信息流（按距离/时间排序）"),
  bullet("筛选条件（舞种、时间、水平）"),
  bullet("发布约练入口"),
  bullet("我的约练（已发布/已参与）"),

  p("社区（Tab 3）", { bold: true }),
  bullet("动态信息流"),
  bullet("话题分类"),
  bullet("活动日历入口"),
  bullet("Workshop专区"),

  p("我的（Tab 4）", { bold: true }),
  bullet("个人信息与舞蹈偏好"),
  bullet("成长档案（数据统计+时间线）"),
  bullet("我的收藏（舞室/课程/老师）"),
  bullet("我的搭子"),
  bullet("我的评价"),
  bullet("训练打卡入口"),
  bullet("设置"),

  new Paragraph({ children: [new PageBreak()] }),

  h2("4.2 核心用户流程"),
  h3("4.2.1 新用户决策流程"),
  p("下载/打开平台 → 授权定位/选择城市 → 浏览首页推荐 → 设置筛选条件 → 浏览舞室列表 → 进入舞室详情 → 查看课程信息 → 阅读评价 → 收藏/预约试听 → （可选）注册登录"),

  h3("4.2.2 约练社交流程"),
  p("进入约练广场 → 浏览约练信息/发布约练 → 响应/等待响应 → 双方确认 → 收到约练提醒 → 完成约练 → 互相评价 → （可选）添加为搭子"),

  h3("4.2.3 学习成长流程"),
  p("完成练舞/上课 → 进入打卡页面 → 填写打卡信息 → 上传视频/照片 → 查看数据更新 → 浏览成长时间线 → 查看月度成长报告 → 分享至社区"),

  h3("4.2.4 商家入驻流程"),
  p("商家发现平台上已有自己的舞室信息 → 申请认领 → 提交资质材料 → 平台审核 → 认领成功 → 完善舞室信息 → 维护课表 → 添加教练 → 管理预约和评价"),

  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第五章 数据实体设计 ====================
children.push(
  h1("五、数据实体关系设计"),

  h2("5.1 核心数据实体"),
  p("以下是平台核心业务涉及的主要数据实体及其关键属性："),

  funcTable(["实体名称", "关键属性", "说明"], [
    ["用户(User)", "ID、手机号、昵称、头像、舞蹈偏好、学习水平、注册时间", "平台所有个人用户"],
    ["舞室(Studio)", "ID、名称、地址、经纬度、营业时间、联系方式、认领状态、主打舞种", "舞蹈培训场所"],
    ["课程(Course)", "ID、所属舞室、名称、舞种、难度、价格、适合人群、训练强度、课程目标", "舞室提供的具体课程"],
    ["教练(Coach)", "ID、姓名、擅长舞种、教学风格、所属舞室、账号类型（全职/签约/自由）", "授课教师"],
    ["课表(Schedule)", "ID、课程ID、教练ID、星期、开始时间、结束时间", "课程的时间安排"],
    ["评价(Review)", "ID、用户ID、对象类型、对象ID、各维度评分、文字内容、图片、权重等级", "用户提交的评价"],
    ["约练(Practice)", "ID、发起者ID、舞种、时间、地点、人数上限、水平要求、状态", "约练发布信息"],
    ["打卡(Checkin)", "ID、用户ID、日期、舞种、时长、地点、内容、媒体文件", "训练打卡记录"],
    ["Workshop", "ID、名称、主办舞室、教练、时间、地点、价格、人数上限、状态", "Workshop活动"],
    ["收藏(Favorite)", "ID、用户ID、对象类型、对象ID、收藏时间", "用户收藏记录"],
    ["搭子关系(Buddy)", "ID、用户A_ID、用户B_ID、建立时间", "练舞伙伴关系"],
  ]),

  h2("5.2 核心实体关系"),
  bullet("用户 ↔ 舞室：多对多（收藏、评价）"),
  bullet("用户 ↔ 课程：多对多（收藏、评价、报名）"),
  bullet("用户 ↔ 教练：多对多（收藏、评价）"),
  bullet("舞室 → 课程：一对多（一个舞室有多门课程）"),
  bullet("舞室 → 教练：多对多（教练可在多个舞室授课）"),
  bullet("课程 → 课表：一对多（一门课程有多个时间段）"),
  bullet("课程 → 教练：多对一（每门课程由一位教练授课）"),
  bullet("用户 ↔ 用户：多对多（搭子关系）"),
  bullet("用户 → 约练：一对多（发起）、多对多（参与）"),
  bullet("用户 → 打卡：一对多"),
  bullet("舞室 → Workshop：一对多"),
  bullet("用户 ↔ Workshop：多对多（报名、签到）"),

  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第六章 非功能需求 ====================
children.push(
  h1("六、非功能需求"),

  h2("6.1 非功能需求概览"),
  nfrTable([
    ["性能", "首页加载时间应控制在合理范围内", "首屏加载 ≤ 2秒（4G网络）"],
    ["性能", "搜索与筛选响应迅速", "搜索结果返回 ≤ 1秒"],
    ["性能", "地图模式流畅加载", "地图渲染 ≤ 1.5秒"],
    ["可用性", "系统高可用", "可用性 ≥ 99.5%"],
    ["安全", "用户隐私数据加密存储", "手机号、密码等敏感信息加密"],
    ["安全", "防止恶意刷评和批量注册", "验证码 + 频率限制 + 行为检测"],
    ["安全", "用户内容合规审核", "UGC内容过审后展示"],
    ["兼容性", "支持主流移动平台", "iOS 14+、Android 10+"],
    ["兼容性", "支持微信小程序", "微信基础库 2.20+"],
    ["可扩展性", "支持多城市扩展", "数据架构支持城市维度隔离"],
    ["可扩展性", "支持舞种分类动态扩展", "舞种分类可配置，无需改代码"],
  ]),

  h2("6.2 数据安全要求"),
  bullet("用户手机号脱敏展示，仅在必要场景（约练确认后）向对方展示"),
  bullet("用户位置信息仅用于搜索和推荐，不对外展示精确位置"),
  bullet("评价内容不可被商家端直接删除，需通过申诉流程"),
  bullet("用户成长数据和打卡记录默认仅自己可见，可选择公开"),
  bullet("支持用户注销账号并清除个人数据"),

  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第七章 MVP与迭代规划 ====================
children.push(
  h1("七、MVP范围与迭代规划"),

  h2("7.1 MVP（第一阶段）核心范围"),
  p("MVP阶段聚焦验证\"搜索决策\"闭环，优先实现以下功能："),

  funcTable(["模块", "MVP功能范围", "验证目标"], [
    ["用户账号", "手机号注册/登录、微信登录、基础个人资料、舞蹈偏好设置", "用户愿意注册并设置偏好"],
    ["舞室搜索", "附近舞室搜索、多维度筛选（舞种/距离/价格）、列表和地图模式", "用户在平台完成舞室发现"],
    ["舞室详情", "基础信息展示、环境图片、课程列表、老师列表", "用户浏览深度（停留时长/课程点击率）"],
    ["课程详情", "课程信息结构化展示、难度标识、适合人群标识", "用户查看课程后收藏或预约的转化率"],
    ["结构化评价", "舞室/老师/课程三维度评价、评分展示、评价权重分层", "用户愿意留下评价（评价率）"],
    ["收藏与预约", "收藏舞室/课程、预约试听", "收藏率和预约转化率"],
  ]),

  h2("7.2 第二阶段：学习陪伴"),
  p("在MVP验证决策闭环成立后，第二阶段补充学习和社交功能："),
  bullet("约练广场：发布约练、响应约练、约练提醒"),
  bullet("训练打卡：基础打卡、学习数据统计、成长时间线"),
  bullet("搭子系统：互加搭子、搭子列表"),
  bullet("消息通知：约练回复、预约确认等系统通知"),

  h2("7.3 第三阶段：生态扩展"),
  p("在用户基础稳固后，第三阶段拓展内容和商业化能力："),
  bullet("内容社区：动态发布、话题标签、点赞评论"),
  bullet("Workshop管理：发布、报名、候补、签到、评价"),
  bullet("商家入驻：舞室认领、课表维护、数据看板"),
  bullet("成长增强：月度报告、成就徽章、阶段作品记录"),
  bullet("拼课功能：发起拼课、凑人开课"),

  h2("7.4 MVP成功指标"),
  funcTable(["指标类别", "具体指标", "目标值"], [
    ["用户获取", "试点区域注册用户数", "≥ 500人（首月）"],
    ["用户活跃", "周活跃用户比例（WAU/注册）", "≥ 30%"],
    ["决策验证", "搜索后查看舞室详情的转化率", "≥ 40%"],
    ["决策验证", "查看详情后收藏或预约的转化率", "≥ 15%"],
    ["评价验证", "上课/试听后提交评价的比例", "≥ 20%"],
    ["留存", "次周留存率", "≥ 25%"],
  ]),

  new Paragraph({ children: [new PageBreak()] }),
);

// ==================== 第八章 附录 ====================
children.push(
  h1("八、附录"),

  h2("附录A：舞种分类参考"),
  funcTable(["一级分类", "二级舞种", "说明"], [
    ["街舞", "Hiphop、Breaking、Locking、Popping、Waacking", "以街头文化为根基的舞蹈类型"],
    ["流行舞", "Jazz、Urban、韩舞/K-pop、现代舞", "流行音乐相关的舞蹈形式"],
    ["中国舞", "古典舞、民族舞、敦煌舞", "中国传统舞蹈"],
    ["拉丁舞", "恰恰、桑巴、伦巴、牛仔舞", "拉丁美洲起源的社交/竞技舞蹈"],
    ["其他", "芭蕾、踢踏舞、肚皮舞、钢管舞", "其他特色舞种"],
  ]),

  h2("附录B：评价维度汇总表"),
  funcTable(["评价对象", "维度1", "维度2", "维度3", "维度4"], [
    ["舞室", "交通便利度", "环境卫生", "场地条件", "整体氛围"],
    ["老师", "耐心程度", "纠错质量", "讲解清晰度", "零基础友好"],
    ["课程", "上手难度", "节奏合理性", "练习强度", "实际收获"],
  ]),
);

// ========== Build document ==========
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: "Microsoft YaHei", size: 21 },
      },
    },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, font: "Microsoft YaHei" },
        paragraph: { spacing: { before: 360, after: 200 }, outlineLevel: 0 },
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, font: "Microsoft YaHei" },
        paragraph: { spacing: { before: 280, after: 160 }, outlineLevel: 1 },
      },
      {
        id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: "Microsoft YaHei" },
        paragraph: { spacing: { before: 200, after: 120 }, outlineLevel: 2 },
      },
    ],
  },
  numbering,
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1417, right: 1417, bottom: 1417, left: 1417 },
      },
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.RIGHT,
          children: [new TextRun({ text: "BitDance \u2014 \u4EA7\u54C1\u9700\u6C42\u5206\u6790\u4E0E\u529F\u80FD\u8BBE\u8BA1\u6587\u6863", font: "Microsoft YaHei", size: 18, color: "999999" })],
        })],
      }),
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [
            new TextRun({ text: "\u7B2C ", font: "Microsoft YaHei", size: 18, color: "999999" }),
            new TextRun({ children: [PageNumber.CURRENT], font: "Microsoft YaHei", size: 18, color: "999999" }),
            new TextRun({ text: " \u9875", font: "Microsoft YaHei", size: 18, color: "999999" }),
          ],
        })],
      }),
    },
    children,
  }],
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("C:\\Users\\patri\\OneDrive - bjtu.edu.cn\\Files\\实训IV\\BitDance-产品需求分析与功能设计文档.docx", buffer);
  console.log("Document created successfully!");
});
