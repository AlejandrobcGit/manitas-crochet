import { useEffect, useState, useCallback } from "react";

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
        error,
        pagina
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
    const [paginaActual, setPaginaActual] = useState(0);
    const [paginaInput, setPaginaInput] = useState("");

    const nombreDebounced = useDebounce(nombre, 400);

    const cargar = useCallback((page) => {
        recargarFiguras({
            nombre: nombreDebounced,
            categoriaId,
            page,
            size: 50
        });
    }, [recargarFiguras, nombreDebounced, categoriaId]);

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
            cargar(paginaActual);
        } catch (err) {
            console.error("Error eliminando figura:", err);
        }
    }

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        setPaginaActual(0);
        cargar(0);
    }, [cargar]);

    const irAPagina = (pagina) => {
        setPaginaActual(pagina);
        cargar(pagina);
    };

    const irAPaginaInput = () => {
        const num = parseInt(paginaInput, 10);
        if (isNaN(num)) return;
        irAPagina(Math.min(Math.max(num - 1, 0), pagina.totalPaginas - 1));
    };

    // Sincroniza el input con la página actual cuando cambia externamente
    useEffect(() => {
        setPaginaInput(String(pagina.paginaActual + 1));
    }, [pagina.paginaActual]);

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

                            {modo === "LISTADO" && pagina.totalPaginas > 1 && (
                                <div className="paginacion">
                                    <button
                                        type="button"
                                        className="paginacion__btn"
                                        disabled={pagina.paginaActual <= 0}
                                        onClick={() => irAPagina(pagina.paginaActual - 1)}
                                    >
                                        ‹ Anterior
                                    </button>
                                    <div className="paginacion__go">
                                        <label
                                            className="paginacion__label"
                                            htmlFor="paginacion-input-admin"
                                        >
                                            Página
                                        </label>
                                        <input
                                            id="paginacion-input-admin"
                                            type="number"
                                            min="1"
                                            max={pagina.totalPaginas}
                                            className="paginacion__input"
                                            value={paginaInput}
                                            onChange={(e) => setPaginaInput(e.target.value)}
                                            onKeyDown={(e) => {
                                                if (e.key === "Enter") irAPaginaInput();
                                            }}
                                        />
                                        <span className="paginacion__info">
                                            de {pagina.totalPaginas} · {pagina.totalElementos} figuras
                                        </span>
                                        <button
                                            type="button"
                                            className="paginacion__btn"
                                            onClick={irAPaginaInput}
                                        >
                                            Ir
                                        </button>
                                    </div>
                                    <button
                                        type="button"
                                        className="paginacion__btn"
                                        disabled={pagina.paginaActual >= pagina.totalPaginas - 1}
                                        onClick={() => irAPagina(pagina.paginaActual + 1)}
                                    >
                                        Siguiente ›
                                    </button>
                                </div>
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