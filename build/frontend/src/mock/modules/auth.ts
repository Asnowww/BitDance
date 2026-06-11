import { mock } from '../index';

const buildMockUser = (phone = '13800000000') => ({
  token: `mock-token-${phone}`,
  user: {
    id: 1,
    phone,
    nickname: '舞者' + phone.slice(-4),
    avatar: '',
    roles: ['user']
  }
});

mock('post', /\/auth\/sms\/send$/, () => ({ sent: true, expiresIn: 60 }));

mock('post', /\/auth\/login$/, ({ data }) => {
  const phone = (data as { phone?: string })?.phone ?? '13800000000';
  return buildMockUser(phone);
});

mock('post', /\/auth\/login\/password$/, ({ data }) => {
  const phone = (data as { phone?: string })?.phone ?? '13800000000';
  return buildMockUser(phone);
});
