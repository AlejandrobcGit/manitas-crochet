const API_URL = "http://localhost:8080";

export const useApiFetch = () => {

    const apiFetch = async (url, options = {}) => {

        const esFormData = options.body instanceof FormData;

        const headers = esFormData
            ? { ...(options.headers || {}) } // sin Content-Type: lo pone el navegador con el boundary por lo. para no romper imagenes
            : {
                "Content-Type": "application/json",
                ...(options.headers || {})
            };

        const response = await fetch(
            `${API_URL}${url}`,
            {
                ...options,
                headers
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