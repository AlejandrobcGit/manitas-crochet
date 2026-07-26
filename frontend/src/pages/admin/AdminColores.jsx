import { useEffect, useState } from "react";

import { useColores } from "../../hooks/useColores";
import { useCategorias } from "../../hooks/useCategorias";
import { useDebounce } from "../../hooks/useDebounce";
import { useApiFetch } from "../../api/useApiFetch";

import "./AdminColores.css";

import AdminColoresTable from "../../components/AdminColoresTable";
import ColorForm from "../../components/ColorForm";

const API_URL = "http://localhost:8080";

function AdminColores() {
    const {
        colores,
        recargarColores,
        loading,
        error
    } = useColores();

    const apiFetch = useApiFetch();

    const [nombre, setNombre] = useState("");
    const [codigo, setCodigo] = useState("");
    const [modo, setModo] = useState("LISTADO");
    const [colorSeleccionado, setColorSeleccionado] = useState(null);

    const nombreDebounced = useDebounce(nombre, 400);
    const codigoDebounced = useDebounce(codigo, 400);

    const onEditar = (color) => {
        setColorSeleccionado(color);
        setModo("EDICION");
    }

    const onEliminar = async (colorId) => {

        try {
            await apiFetch(`/api/color/${colorId}`, {
                method: "DELETE"
            });

            setColorSeleccionado("");
            setModo("LISTADO");
            recargarColores(nombreDebounced, codigoDebounced);
        } catch (err) {
            console.error("Error eliminando color:", err);
        }
    }

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarColores(nombreDebounced, codigoDebounced);
    }, [nombreDebounced, codigoDebounced]);

    return (
        <>
            {modo === "LISTADO" && (
                <div className="admin-filtros">

                    <label className="admin-campo">
                        <span>Color</span>

                        <input
                            type="text"
                            className="admin-busqueda"
                            placeholder="Buscar..."
                            value={nombre}
                            onChange={(e) => setNombre(e.target.value)}
                        />
                    </label>

                    <label className="admin-campo">
                        <span>Codigo</span>

                        <input
                            type="text"
                            className="admin-busqueda"
                            placeholder="Buscar..."
                            value={codigo}
                            onChange={(e) => setCodigo(e.target.value)}
                        />
                    </label>

                    <button
                        type="button"
                        className="admin-limpiar"
                        onClick={() => {
                            setNombre("");
                            setCodigo("");
                        }}
                    >
                        Limpiar
                    </button>

                    <button className="admin-limpiar"
                        onClick={() => setModo("CREAR")}
                    >
                        Crear Color
                    </button>
                </div>
            )}
            <div className="catalog-content">
                {loading && (
                    <p className="catalog-status">
                        Cargando colores...
                    </p>
                )}

                {error && (
                    <p className="catalog-status catalog-status--error">
                        Error al cargar colores
                    </p>
                )}

                {!loading &&
                    !error && (
                        <div>
                            {modo === "LISTADO" && (
                                <AdminColoresTable
                                    colores={colores}
                                    onEditar={onEditar}
                                    onEliminar={onEliminar}
                                />
                            )}

                            {modo === "CREAR" && (
                                <ColorForm
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                            {modo === "EDICION" && (
                                <ColorForm
                                    esEdicion={true}
                                    color={colorSeleccionado}
                                    onVolver={() => setModo("LISTADO")}
                                />
                            )}

                        </div>
                    )}

                {!loading &&
                    !error &&
                    colores.length === 0 && (

                        <p className="catalog-status">
                            No se encontraron colores.
                        </p>

                    )}
            </div>
        </>
    );
}

export default AdminColores;