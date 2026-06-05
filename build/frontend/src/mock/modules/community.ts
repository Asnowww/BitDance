import { mock } from '../index';

const POST_KEY = 'bitdance_mock_posts';
const COMMENT_KEY = 'bitdance_mock_comments';
const FOLLOW_KEY = 'bitdance_mock_follow';

interface Post {
  id: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  hasVideo?: boolean;
  topics: string[];
  style?: string;
  location?: string;
  likeCount: number;
  commentCount: number;
  collectCount: number;
  liked: boolean;
  collected: boolean;
  createdAt: number;
}

interface Comment {
  id: number;
  postId: number;
  authorId: number;
  authorName: string;
  text: string;
  createdAt: number;
}

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const TOPICS = ['零基础打卡', '街舞日常', '试听感受', '舞室探店', 'Workshop 速记'];
const NICK = ['小喵', '舞月', '阿橘', '云朵', '羊羊', '团团', 'Aki'];

const seed = (): Post[] => {
  const out: Post[] = [];
  for (let i = 1; i <= 24; i += 1) {
    const tArr = TOPICS.slice(i % 4, (i % 4) + 1 + (i % 3));
    out.push({
      id: i,
      authorId: 200 + i,
      authorName: NICK[i % NICK.length],
      authorAvatar: '',
      text: ['今日打卡：', '试听记录：', 'Workshop 复盘：', '搭子招募：'][i % 4] + STYLES[i % STYLES.length] + ' 状态在线 🎶',
      images: i % 3 === 0 ? [] : ['p1', 'p2'].slice(0, (i % 2) + 1),
      hasVideo: i % 5 === 0,
      topics: tArr,
      style: STYLES[i % STYLES.length],
      location: ['海淀区舞星 Studio', '朝阳区灵动空间', '浦东新区舞蹈坊'][i % 3],
      likeCount: 5 + i * 3,
      commentCount: i % 5,
      collectCount: i % 7,
      liked: false,
      collected: false,
      createdAt: Date.now() - i * 7200_000
    });
  }
  return out;
};

const loadPosts = (): Post[] => {
  try {
    const raw = localStorage.getItem(POST_KEY);
    return raw ? (JSON.parse(raw) as Post[]) : seed();
  } catch {
    return seed();
  }
};
const savePosts = (items: Post[]) => localStorage.setItem(POST_KEY, JSON.stringify(items));

const loadComments = (): Comment[] => {
  try {
    return JSON.parse(localStorage.getItem(COMMENT_KEY) ?? '[]') as Comment[];
  } catch {
    return [];
  }
};
const saveComments = (items: Comment[]) => localStorage.setItem(COMMENT_KEY, JSON.stringify(items));

const loadFollow = (): number[] => {
  try {
    return JSON.parse(localStorage.getItem(FOLLOW_KEY) ?? '[]') as number[];
  } catch {
    return [];
  }
};
const saveFollow = (ids: number[]) => localStorage.setItem(FOLLOW_KEY, JSON.stringify(ids));

mock('get', /\/community\/feed$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = loadPosts();
  if (p.topic) items = items.filter((it) => it.topics.includes(p.topic as string));
  if (p.style) items = items.filter((it) => it.style === p.style);
  if (p.scope === 'follow') {
    const followed = loadFollow();
    items = items.filter((it) => followed.includes(it.authorId));
  }
  items = items.slice().sort((a, b) => b.createdAt - a.createdAt);
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
});

mock('get', /\/community\/posts\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return loadPosts().find((it) => it.id === id) ?? null;
});

mock('post', /\/community\/posts$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = loadPosts();
  const item: Post = {
    id: Date.now(),
    authorId: 999,
    authorName: '我',
    authorAvatar: '',
    text: (body.text as string) ?? '',
    images: (body.images as string[]) ?? [],
    hasVideo: Boolean(body.hasVideo),
    topics: (body.topics as string[]) ?? [],
    style: body.style as string | undefined,
    location: body.location as string | undefined,
    likeCount: 0,
    commentCount: 0,
    collectCount: 0,
    liked: false,
    collected: false,
    createdAt: Date.now()
  };
  items.unshift(item);
  savePosts(items);
  return item;
});

mock('post', /\/community\/posts\/\d+\/like$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { liked: false, likeCount: 0 };
  items[idx].liked = !items[idx].liked;
  items[idx].likeCount += items[idx].liked ? 1 : -1;
  savePosts(items);
  return { liked: items[idx].liked, likeCount: items[idx].likeCount };
});

mock('post', /\/community\/posts\/\d+\/collect$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { collected: false, collectCount: 0 };
  items[idx].collected = !items[idx].collected;
  items[idx].collectCount += items[idx].collected ? 1 : -1;
  savePosts(items);
  return { collected: items[idx].collected, collectCount: items[idx].collectCount };
});

mock('post', /\/community\/posts\/\d+\/report$/, () => ({ reported: true }));

mock('get', /\/community\/posts\/\d+\/comments$/, ({ url }) => {
  const postId = Number(url.split('/').slice(-2)[0]);
  return loadComments()
    .filter((c) => c.postId === postId)
    .sort((a, b) => a.createdAt - b.createdAt);
});

mock('post', /\/community\/posts\/\d+\/comments$/, ({ url, data }) => {
  const postId = Number(url.split('/').slice(-2)[0]);
  const body = (data ?? {}) as Record<string, unknown>;
  const comments = loadComments();
  const c: Comment = {
    id: Date.now(),
    postId,
    authorId: 999,
    authorName: '我',
    text: (body.text as string) ?? '',
    createdAt: Date.now()
  };
  comments.push(c);
  saveComments(comments);
  // 更新 post 评论数
  const posts = loadPosts();
  const idx = posts.findIndex((it) => it.id === postId);
  if (idx >= 0) {
    posts[idx].commentCount = comments.filter((x) => x.postId === postId).length;
    savePosts(posts);
  }
  return c;
});

mock('get', /\/community\/topics$/, () => {
  const posts = loadPosts();
  const counts = new Map<string, number>();
  posts.forEach((p) => p.topics.forEach((t) => counts.set(t, (counts.get(t) ?? 0) + 1)));
  return Array.from(counts.entries())
    .map(([name, count]) => ({ name, count, hot: count >= 4 }))
    .sort((a, b) => b.count - a.count);
});

mock('post', /\/community\/follow\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const followed = loadFollow();
  const i = followed.indexOf(id);
  if (i >= 0) {
    followed.splice(i, 1);
    saveFollow(followed);
    return { following: false };
  }
  followed.push(id);
  saveFollow(followed);
  return { following: true };
});

mock('get', /\/community\/follow\/me$/, () => {
  const followed = new Set(loadFollow());
  const posts = loadPosts();
  const seen = new Set<number>();
  const out: Array<{ id: number; name: string; avatar: string; followed: boolean }> = [];
  posts.forEach((p) => {
    if (!seen.has(p.authorId)) {
      seen.add(p.authorId);
      out.push({
        id: p.authorId,
        name: p.authorName,
        avatar: p.authorAvatar,
        followed: followed.has(p.authorId)
      });
    }
  });
  return out;
});

mock('get', /\/community\/search$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const q = ((p.q as string) ?? '').toLowerCase();
  const items = loadPosts().filter(
    (it) =>
      it.text.toLowerCase().includes(q) ||
      it.topics.some((t) => t.toLowerCase().includes(q)) ||
      (it.style ?? '').toLowerCase().includes(q) ||
      it.authorName.toLowerCase().includes(q)
  );
  return { list: items, page: 1, pageSize: items.length, total: items.length };
});

mock('get', /\/public\/users\/\d+\/community\/posts$/, ({ url, params }) => {
  const userId = Number(url.split('/').slice(-3)[0]);
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 10);
  let items = loadPosts().filter((it) => it.authorId === userId);
  if (items.length === 0 && userId === 1) {
    items = [
      {
        id: 9101,
        authorId: 1,
        authorName: '小李',
        authorAvatar: '',
        text: '今天试听了 Urban Flow 的韩舞课，老师会拆动作，零基础也跟得上。',
        images: [],
        topics: ['Urban Flow', '韩舞'],
        style: '韩舞',
        location: '北京海淀',
        likeCount: 38,
        commentCount: 6,
        collectCount: 3,
        liked: false,
        collected: false,
        createdAt: Date.now() - 3600_000
      }
    ];
  }
  items = items.slice().sort((a, b) => b.createdAt - a.createdAt);
  const start = (page - 1) * pageSize;
  return { list: items.slice(start, start + pageSize), page, pageSize, total: items.length };
});
