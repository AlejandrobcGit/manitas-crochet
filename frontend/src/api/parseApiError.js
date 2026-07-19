export const parseApiError = async (response) => {
    try {
        const data = await response.json();
        if (data?.error && data?.mensaje) {
            return data;
        }
    } catch {
        // La respuesta no tenía JSON
    }
    return {
        status: response.status,
        error: "UNKNOWN_ERROR",
        mensaje: "Ha ocurrido un error inesperado.",
        timestamp: new Date().toISOString(),
        fieldErrors: null
    };
};