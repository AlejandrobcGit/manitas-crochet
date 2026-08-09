import { useEffect, useState } from "react";

import { useFiguras } from "../../hooks/useFiguras";
import { useCategorias } from "../../hooks/useCategorias";
import { useDebounce } from "../../hooks/useDebounce";
import { useApiFetch } from "../../api/useApiFetch";

import "./AdminFiguras.css";

import AdminFigurasTable from "../../components/AdminFigurasTable";
import FiguraForm from "../../components/FiguraForm";

function AdminFiguras() {
    const {
        figuras,
        recargarFiguras,
        loading,
        error
    } = useFiguras();

    const apiFetch = useApiFetch();

    const {
        categorias,
        loading: loadingCategorias
    } = useCategorias();

    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");
    const [modo, setModo] = useState("LISTADO");
    const [figuraSeleccionada, setFiguraSeleccionada] = useState(null);

    const nombreDebounced = useDebounce(nombre, 400);

    const onEditar = (figuraId) => {
        setFiguraSeleccionada(figuraId);
        setModo("EDICION");
    }

    const onEliminar = async (figuraId) => {

        try {
            await apiFetch(`/api/figuras/${figuraId}`, {
                method: "DELETE"
            });

            setFiguraSeleccionada("");
            setModo("LISTADO");
            recargarFiguras(nombreDebounced, categoriaId);
        } catch (err) {
            console.error("Error eliminando figura:", err);
        }
    }

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarFiguras(nombreDebounced, categoriaId);
    }, [nombreDebounced, categoriaId, recargarFiguras]);

    return (
        <>
            {modo === "LISTADO" && (
                <div className="admin-filtros">

                    <label className="admin-campo">
                        <span>Nombre</span>

                        <input
                            type="text"
                            className="admin-busqueda"
                            placeholder="Buscar..."
                            value={nombre}
                            onChange={(e) => setNombre(e.target.value)}
                        />
                    </label>

                    {!loadingCategorias && (
                        <>
                            <label className="admin-campo">
                                <span>Categoría</span>

                                <select
                                    className="admin-select"
                                    value={categoriaId}
                                    onChange={(e) => setCategoriaId(e.target.value)}
                                >
                                    <option value="">
                                        Todas
                                    </option>

                                    {categorias.map((cat) => (
                                        <option
                                            key={cat.id}
                                            value={cat.id}
                                        >
                                            {cat.nombre}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <button
                                type="button"
                                className="admin-limpiar"
                                onClick={() => {
                                    setNombre("");
                                    setCategoriaId("");
                                }}
                            >
                                Limpiar
                            </button>

                            <button className="admin-limpiar"
                                onClick={() => setModo("CREAR")}
                            >
                                Crear figura
                            </button>

                        </>
                    )}

                </div>
            )}
            <div className="catalog-content">
                {loading && (
                    <p className="catalog-status">
                        Cargando figuras...
                    </p>
                )}

                {error && (
                    <p className="catalog-status catalog-status--error">
                        Error al cargar figuras
                    </p>
                )}

                {!loading &&
                    !error && (
                        <div>
                            {modo === "LISTADO" && (
                                <AdminFigurasTable
                                    figuras={figuras}
                                    onEditar={onEditar}
                                    onEliminar={onEliminar}
                                />
                            )}

                            {modo === "CREAR" && (
                                <FiguraForm
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                            {modo === "EDICION" && (
                                <FiguraForm
                                    esEdicion={true}
                                    figuraId={figuraSeleccionada}
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                        </div>
                    )}

                {!loading &&
                    !error &&
                    figuras.length === 0 && (

                        <p className="catalog-status">
                            No se encontraron figuras.
                        </p>

                    )}
            </div>
        </>
    );
}

export default AdminFiguras;