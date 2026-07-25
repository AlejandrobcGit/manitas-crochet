import { useEffect, useState } from "react";

import { useFiguras } from "../../hooks/useFiguras";
import { useCategorias } from "../../hooks/useCategorias";
import { useDebounce } from "../../hooks/useDebounce";

import "./AdminFiguras.css";

import AdminFigurasTable from "../../components/AdminFigurasTable";

function AdminFiguras() {
    const {
        figuras,
        recargarFiguras,
        loading,
        error
    } = useFiguras();

    const {
        categorias,
        loading: loadingCategorias
    } = useCategorias();

    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");

    const nombreDebounced = useDebounce(nombre, 400);

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarFiguras(nombreDebounced, categoriaId);
    }, [nombreDebounced, categoriaId]);

    return (
        <>
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

                        <button className="btn-editar">
                            Crear figura
                        </button>

                    </>
                )}

            </div>
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

                            <AdminFigurasTable
                                figuras={figuras}
                                onEditar={(figura) => {
                                    console.log("Editar", figura);
                                }}
                                onEliminar={(figura) => {
                                    console.log("Eliminar", figura);
                                }}
                            />

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