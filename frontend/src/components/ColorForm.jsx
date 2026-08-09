import { useContext, useEffect, useState } from "react";

import { useApiFetch } from "../api/useApiFetch";
import { ColoresContext } from "../contexts/ColoresContextDefinition";

import "./ColorForm.css";

function ColorForm({ onVolver, esEdicion = false, color = "#000000" }) {

    const apiFetch = useApiFetch();

    const { recargarColores } = useContext(ColoresContext);
    const [nombre, setNombre] = useState("");
    const [codigo, setCodigo] = useState(color);
    const [cargandoColor] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState(null);


    useEffect(() => {

        if (!esEdicion) {
            return;
        }

        setNombre(color.nombre)
        setCodigo(color.codigo)

    }, [esEdicion, color]);


    const resetForm = () => {
        setNombre("");
        setCodigo("");
    };

    const handleSubmit = async (event) => {

        event.preventDefault();

        if (!nombre.trim()) {
            setError("El nombre es obligatorio");
            return;
        }

        if (!codigo) {
            setError("Codigo es obligatorio");
            return;
        }

        setError(null);
        setEnviando(true);

        try {

            const colorEditar = {
                nombre: nombre.trim(),
                codigo
            };

            const endpoint = esEdicion
                ? `/api/color/${color.id}`
                : "/api/color";

            const method = esEdicion
                ? "PUT"
                : "POST";

            const response = await apiFetch(endpoint, {
                method,
                body: JSON.stringify(colorEditar)
            });

            /*const colorGuardada =*/ await response.json();

            await recargarColores?.();

            if (!esEdicion) {
                resetForm();
            }

            onVolver?.();

        } catch (err) {

            setError(
                err.mensaje || err.message ||
                (
                    esEdicion
                        ? "Error al actualizar el color"
                        : "Error al crear el color"
                )
            );

        } finally {

            setEnviando(false);
        }
    };

    if (cargandoColor) {

        return (
            <main className="form-main">
                <p className="form-status">
                    Cargando color...
                </p>
            </main>
        );
    }

    return (
        <main className="form-main">
            <h1 className="form-title">
                {esEdicion ? "Editar color" : "Nuevo color"}
            </h1>

            <form className="color-form" onSubmit={handleSubmit}>

                {error && (
                    <p className="form-status form-status--error">
                        {error}
                    </p>
                )}

                <div className="form-columns">
                    <div className="form-left">
                        <div className="form-grid">

                            <label className="form-field">
                                <span>Nombre *</span>

                                <input
                                    type="text"
                                    value={nombre}
                                    onChange={event => setNombre(event.target.value)}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>Codigo</span>

                                <input
                                    type="color"
                                    value={codigo}
                                    onChange={event => setCodigo(event.target.value)}
                                />
                            </label>
                        </div>

                    </div>

                    <aside className="form-right">
                        <div className="form-actions">
                            <button
                                type="submit"
                                className="form-btn form-btn--principal"
                                disabled={enviando}
                            >
                                {enviando
                                    ? esEdicion
                                        ? "Guardando…"
                                        : "Creando…"
                                    : esEdicion
                                        ? "Guardar cambios"
                                        : "Crear color"}
                            </button>

                            <button
                                type="button"
                                className="form-btn form-btn--secundario"
                                onClick={onVolver}
                                disabled={enviando}
                            >
                                Cancelar
                            </button>
                        </div>

                    </aside>
                </div>
            </form>
        </main>
    );
}

export default ColorForm;