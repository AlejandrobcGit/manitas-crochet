import { useContext, useEffect, useState } from "react";

import { useApiFetch } from "../api/useApiFetch";
import { CategoriasContext } from "../contexts/CategoriasContextDefinition";

import "./CategoriaForm.css";

function CategoriaForm({ onVolver, esEdicion = false, categoria = null }) {

    const apiFetch = useApiFetch();

    const { recargarCategorias } = useContext(CategoriasContext);
    const [nombre, setNombre] = useState("");
    const [cargandoCategoria] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState(null);


    useEffect(() => {

        if (!esEdicion || !categoria) {
            return;
        }

        setNombre(categoria.nombre);

    }, [esEdicion, categoria]);


    const resetForm = () => {
        setNombre("");
    };

    const handleSubmit = async (event) => {

        event.preventDefault();

        if (!nombre.trim()) {
            setError("El nombre es obligatorio");
            return;
        }

        setError(null);
        setEnviando(true);

        try {

            const categoriaEditar = {
                nombre: nombre.trim(),
            };

            const endpoint = esEdicion
                ? `/api/categorias/${categoria.id}`
                : "/api/categorias";

            const method = esEdicion
                ? "PUT"
                : "POST";

            const response = await apiFetch(endpoint, {
                method,
                body: JSON.stringify(categoriaEditar)
            });

            /*const categoriaGuardada = */

            await response.json();

            await recargarCategorias?.();

            if (!esEdicion) {
                resetForm();
            }

            onVolver?.();

        } catch (err) {

            setError(
                err.mensaje || err.message ||
                (
                    esEdicion
                        ? "Error al actualizar el categoria"
                        : "Error al crear el categoria"
                )
            );

        } finally {

            setEnviando(false);
        }
    };

    if (cargandoCategoria) {

        return (
            <main className="form-main">
                <p className="form-status">
                    Cargando categoria...
                </p>
            </main>
        );
    }

    return (
        <main className="form-main">
            <h1 className="form-title">
                {esEdicion ? "Editar categoria" : "Nuevo categoria"}
            </h1>

            <form className="categoria-form" onSubmit={handleSubmit}>

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
                                        : "Crear categoria"}
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

export default CategoriaForm;