import { useEffect, useState } from "react";

/*
cada vez que valor cambia (por ejemplo, el usuario teclea), reinicia un temporizador. 
Solo cuando el usuario deja de escribir durante delayMs (400ms) se actualiza valorDebounced, 
que es lo que realmente dispara la petición al backend.
*/

export function useDebounce(valor, delayMs = 400) {

    const [valorDebounced, setValorDebounced] = useState(valor);

    useEffect(() => {

        const timeoutId = setTimeout(() => {
            setValorDebounced(valor);
        }, delayMs);

        return () => clearTimeout(timeoutId);

    }, [valor, delayMs]);

    return valorDebounced;
}