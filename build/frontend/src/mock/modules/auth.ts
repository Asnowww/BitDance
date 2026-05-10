import { mock } from '../index';

mock('post', /\/auth\/sms\/send$/, () => ({ sent: true, expiresIn: 60 }));

mock('post', /\/auth\/login$/, ({ data }) => {
  const phone = (data as { phone?: string })?.phone ?? '13800000000';
  return {
    token: `mock-token-${phone}`,
    user: {
      id: 1,
      phone,
      nickname: '舞者' + phone.slice(-4),
      avatar: '',
      roles: ['user']
    }
  };
});
