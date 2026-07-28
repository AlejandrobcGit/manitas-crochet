export const parseApiError = async (response) => {
    let data = null;

    try {
        data = await response.json();
    } catch (parseErr) {
        console.warn("parseApiError: la respuesta no contenía JSON válido", parseErr);
    }

    return {
        status: response.status, // ⬅️ SIEMPRE del response real, nunca confíes en que venga en el body
        error: data?.error || "UNKNOWN_ERROR",
        mensaje: data?.mensaje || "Ha ocurrido un error inesperado.",
        timestamp: data?.timestamp || new Date().toISOString(),
        fieldErrors: data?.fieldErrors || null
    };
};