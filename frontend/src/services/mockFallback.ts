export class ApiUnavailableError extends Error {
  endpoint: string;

  constructor(feature: string, endpoint: string) {
    super(`${feature} 接口暂未提供：${endpoint}`);
    this.name = "ApiUnavailableError";
    this.endpoint = endpoint;
  }
}

export const rejectUnavailableApi = <T>(
  feature: string,
  endpoint: string
): Promise<T> => Promise.reject(new ApiUnavailableError(feature, endpoint));
