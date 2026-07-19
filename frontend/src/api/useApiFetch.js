const API_URL = "http://localhost:8080";

export const useApiFetch = () => {

    const apiFetch = async (url, options = {}) => {

        const response = await fetch(
            `${API_URL}${url}`,
            {
                ...options,
                headers: {
                    "Content-Type": "application/json",
                    ...(options.headers || {})
                }
            }
        );

        if (!response.ok) {

            const error = await response.json();

            throw error;
        }

        return response;
    };

    return apiFetch;
};