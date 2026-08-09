import { useEffect, useState } from "react";

import { useCategorias } from "../../hooks/useCategorias";
import { useDebounce } from "../../hooks/useDebounce";
import { useApiFetch } from "../../api/useApiFetch";

import "./AdminCategorias.css";

import AdminCategoriasTable from "../../components/AdminCategoriasTable";
import CategoriaForm from "../../components/CategoriaForm";


function AdminCategorias() {
    const {
        categorias,
        recargarCategorias,
        loading,
        error
    } = useCategorias();

    const apiFetch = useApiFetch();

    const [nombre, setNombre] = useState("");
    const [modo, setModo] = useState("LISTADO");
    const [categoriaSeleccionado, setCategoriaSeleccionado] = useState(null);

    const nombreDebounced = useDebounce(nombre, 400);

    const onEditar = (categoria) => {
        setCategoriaSeleccionado(categoria);
        setModo("EDICION");
    }

    const onEliminar = async (categoriaId) => {

        try {
            await apiFetch(`/api/categorias/${categoriaId}`, {
                method: "DELETE"
            });

            setCategoriaSeleccionado("");
            setModo("LISTADO");
            recargarCategorias(nombreDebounced);
        } catch (err) {
            console.error("Error eliminando categoria:", err);
        }
    }

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarCategorias(nombreDebounced);
    }, [nombreDebounced, recargarCategorias]);

    return (
        <>
            {modo === "LISTADO" && (
                <div className="admin-filtros">

                    <label className="admin-campo">
                        <span>Categoria</span>

                        <input
                            type="text"
                            className="admin-busqueda"
                            placeholder="Buscar..."
                            value={nombre}
                            onChange={(e) => setNombre(e.target.value)}
                        />
                    </label>
                    <button
                        type="button"
                        className="admin-limpiar"
                        onClick={() => {
                            setNombre("");
                        }}
                    >
                        Limpiar
                    </button>

                    <button className="admin-limpiar"
                        onClick={() => setModo("CREAR")}
                    >
                        Crear Categoria
                    </button>
                </div>
            )}
            <div className="catalog-content">
                {loading && (
                    <p className="catalog-status">
                        Cargando categorias...
                    </p>
                )}

                {error && (
                    <p className="catalog-status catalog-status--error">
                        Error al cargar categorias
                    </p>
                )}

                {!loading &&
                    !error && (
                        <div>
                            {modo === "LISTADO" && (
                                <AdminCategoriasTable
                                    categorias={categorias}
                                    onEditar={onEditar}
                                    onEliminar={onEliminar}
                                />
                            )}

                            {modo === "CREAR" && (
                                <CategoriaForm
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                            {modo === "EDICION" && (
                                <CategoriaForm
                                    esEdicion={true}
                                    categoria={categoriaSeleccionado}
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                        </div>
                    )}

                {!loading &&
                    !error &&
                    categorias.length === 0 && (

                        <p className="catalog-status">
                            No se encontraron categorias.
                        </p>

                    )}
            </div>
        </>
    );
}

export default AdminCategorias;