import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { useApiFetch } from "../api/useApiFetch";
import { FigurasContext } from "../contexts/FigurasContext";

import Header from "../components/Header";
import Footer from "../components/Footer";

import "./FiguraForm.css";

const API_URL = "http://localhost:8080";

const DIFICULTADES = [
    "PRINCIPIANTE",
    "INTERMEDIO",
    "AVANZADO"
];

function FiguraForm() {

    const apiFetch = useApiFetch();
    const navigate = useNavigate();
    const { id } = useParams();

    const esEdicion = Boolean(id);

    const { recargarFiguras } = useContext(FigurasContext);

    const [categorias, setCategorias] = useState([]);
    const [coloresDisponibles, setColoresDisponibles] = useState([]);
    const [cargandoOpciones, setCargandoOpciones] = useState(true);
    const [errorOpciones, setErrorOpciones] = useState(null);

    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");
    const [dificultad, setDificultad] = useState("");
    const [descripcion, setDescripcion] = useState("");
    const [altura, setAltura] = useState("");
    const [ancho, setAncho] = useState("");
    const [autor, setAutor] = useState("");
    const [peso, setPeso] = useState("");

    const [coloresIds, setColoresIds] = useState([]);

    const [comboColoresAbierto, setComboColoresAbierto] = useState(false);
    const [filtroColor, setFiltroColor] = useState("");
    const comboColoresRef = useRef(null);

    const [imagenPrincipal, setImagenPrincipal] = useState(null);
    const [imagenPrincipalPreview, setImagenPrincipalPreview] = useState(null);

    const [imagenesSecundarias, setImagenesSecundarias] = useState([]);
    const [imagenesSecundariasPreview, setImagenesSecundariasPreview] = useState([]);

    const [cargandoFigura, setCargandoFigura] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState(null);

    const crearUrlImagen = (filename) => {

        if (!filename) {
            return null;
        }

        if (filename.startsWith("http")) {
            return filename;
        }

        return `${API_URL}/api/imagenes/${filename}`;
    };

    useEffect(() => {

        const cargarOpciones = async () => {

            try {

                setCargandoOpciones(true);
                setErrorOpciones(null);

                const [resCategorias, resColores] = await Promise.all([
                    apiFetch("/api/categorias"),
                    apiFetch("/api/color")
                ]);

                const [dataCategorias, dataColores] = await Promise.all([
                    resCategorias.json(),
                    resColores.json()
                ]);

                setCategorias(dataCategorias);
                setColoresDisponibles(dataColores);

            } catch (err) {

                setErrorOpciones("No se pudieron cargar categorías/colores");

            } finally {

                setCargandoOpciones(false);
            }
        };

        cargarOpciones();

    }, []);

    useEffect(() => {

        if (!esEdicion) {
            return;
        }

        const cargarFigura = async () => {

            try {

                setCargandoFigura(true);
                setError(null);

                const response = await apiFetch(`/api/figuras/${id}`);
                const figura = await response.json();

                setNombre(figura.nombre || "");
                setDescripcion(figura.descripcion || "");
                setDificultad(figura.dificultad || "");
                setAutor(figura.autor || "");
                setAltura(figura.altura ?? "");
                setAncho(figura.ancho ?? "");
                setPeso(figura.peso ?? "");

                if (figura.categoriaId) {

                    setCategoriaId(figura.categoriaId);

                } else if (figura.categoria && categorias.length > 0) {

                    const categoriaEncontrada = categorias.find(
                        categoria => categoria.nombre === figura.categoria
                    );

                    setCategoriaId(categoriaEncontrada?.id || "");
                }

                if (figura.coloresIds) {

                    setColoresIds(figura.coloresIds);

                } else if (figura.colores && coloresDisponibles.length > 0) {

                    const idsColores = figura.colores
                        .map(colorDetalle => {

                            const colorEncontrado = coloresDisponibles.find(
                                color =>
                                    color.nombre === colorDetalle.nombre ||
                                    color.codigo === colorDetalle.codigo
                            );

                            return colorEncontrado?.id;
                        })
                        .filter(Boolean);

                    setColoresIds(idsColores);
                }

                if (figura.imagenPrincipal) {
                    setImagenPrincipalPreview(
                        crearUrlImagen(figura.imagenPrincipal)
                    );
                }

                if (figura.imagenesSecundarias) {
                    setImagenesSecundariasPreview(
                        figura.imagenesSecundarias.map(
                            imagen => crearUrlImagen(imagen)
                        )
                    );
                }

            } catch (err) {

                setError(err.message || "No se pudo cargar la figura");

            } finally {

                setCargandoFigura(false);
            }
        };

        cargarFigura();

    }, [id, esEdicion, categorias, coloresDisponibles]);

    useEffect(() => {

        const handleClickFuera = (event) => {

            if (
                comboColoresRef.current &&
                !comboColoresRef.current.contains(event.target)
            ) {
                setComboColoresAbierto(false);
            }
        };

        document.addEventListener("mousedown", handleClickFuera);

        return () => {
            document.removeEventListener("mousedown", handleClickFuera);
        };

    }, []);

    const coloresOrdenados = useMemo(() => {

        const texto = filtroColor.trim().toLowerCase();

        const filtrados = texto
            ? coloresDisponibles.filter(color =>
                color.nombre.toLowerCase().includes(texto)
            )
            : coloresDisponibles;

        return [...filtrados].sort((a, b) => {

            const aSeleccionado = coloresIds.includes(a.id);
            const bSeleccionado = coloresIds.includes(b.id);

            if (aSeleccionado && !bSeleccionado) {
                return -1;
            }

            if (!aSeleccionado && bSeleccionado) {
                return 1;
            }

            if (aSeleccionado && bSeleccionado) {
                return coloresIds.indexOf(a.id) - coloresIds.indexOf(b.id);
            }

            return a.nombre.localeCompare(b.nombre);
        });

    }, [coloresDisponibles, coloresIds, filtroColor]);

    const coloresSeleccionados = useMemo(() => {

        return coloresIds
            .map(colorId =>
                coloresDisponibles.find(color => color.id === colorId)
            )
            .filter(Boolean);

    }, [coloresIds, coloresDisponibles]);

    const handleImagenPrincipal = (event) => {

        const file = event.target.files?.[0] || null;

        setImagenPrincipal(file);

        setImagenPrincipalPreview(
            file ? URL.createObjectURL(file) : null
        );
    };

    const handleImagenesSecundarias = (event) => {

        const files = Array.from(event.target.files || []);

        setImagenesSecundarias(files);

        setImagenesSecundariasPreview(
            files.map(file => URL.createObjectURL(file))
        );
    };

    const toggleColor = (idColor) => {

        setColoresIds(prev =>
            prev.includes(idColor)
                ? prev.filter(colorId => colorId !== idColor)
                : [...prev, idColor]
        );
    };

    const resetForm = () => {

        setNombre("");
        setCategoriaId("");
        setDificultad("");
        setDescripcion("");
        setAltura("");
        setAncho("");
        setPeso("");
        setAutor("");
        setColoresIds([]);

        setImagenPrincipal(null);
        setImagenPrincipalPreview(null);

        setImagenesSecundarias([]);
        setImagenesSecundariasPreview([]);
    };

    const handleSubmit = async (event) => {

        event.preventDefault();

        if (!nombre.trim()) {
            setError("El nombre es obligatorio");
            return;
        }

        if (!categoriaId) {
            setError("La categoría es obligatoria");
            return;
        }

        if (!dificultad) {
            setError("La dificultad es obligatoria");
            return;
        }

        if (coloresIds.length === 0) {
            setError("Debes seleccionar al menos un color");
            return;
        }

        if (!esEdicion && !imagenPrincipal) {
            setError("La imagen principal es obligatoria");
            return;
        }

        setError(null);
        setEnviando(true);

        try {

            const figura = {
                nombre: nombre.trim(),
                categoriaId,
                dificultad,
                descripcion: descripcion.trim() || null,
                altura: altura ? Number(altura) : null,
                ancho: ancho ? Number(ancho) : null,
                peso: peso ? Number(peso) : null,
                autor: autor.trim() || null,
                coloresIds
            };

            const formData = new FormData();

            formData.append(
                "data",
                new Blob(
                    [JSON.stringify(figura)],
                    { type: "application/json" }
                )
            );

            if (imagenPrincipal) {
                formData.append("imagenPrincipal", imagenPrincipal);
            }

            imagenesSecundarias.forEach(file => {
                formData.append("imagenesSecundarias", file);
            });

            const endpoint = esEdicion
                ? `/api/figuras/${id}`
                : "/api/figuras";

            const method = esEdicion
                ? "PUT"
                : "POST";

            const response = await apiFetch(endpoint, {
                method,
                body: formData
            });

            const figuraGuardada = await response.json();

            await recargarFiguras?.();

            if (!esEdicion) {
                resetForm();
            }

            navigate(`/figuras/${figuraGuardada.id}`);

        } catch (err) {

            setError(
                err.message ||
                (
                    esEdicion
                        ? "Error al actualizar la figura"
                        : "Error al crear la figura"
                )
            );

        } finally {

            setEnviando(false);
        }
    };

    if (cargandoFigura) {

        return (
            <div className="app">
                <Header />

                <main className="form-main">
                    <p className="form-status">
                        Cargando figura...
                    </p>
                </main>

                <Footer />
            </div>
        );
    }

    return (
        <div className="app">
            <Header />

            <main className="form-main">
                <h1 className="form-title">
                    {esEdicion ? "Editar figura" : "Nueva figura"}
                </h1>

                <form className="figura-form" onSubmit={handleSubmit}>

                    {error && (
                        <p className="form-status form-status--error">
                            {error}
                        </p>
                    )}

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
                            <span>Categoría *</span>

                            <select
                                value={categoriaId}
                                onChange={event => setCategoriaId(event.target.value)}
                                disabled={cargandoOpciones}
                                required
                            >
                                <option value="">Selecciona…</option>

                                {categorias.map(categoria => (
                                    <option
                                        key={categoria.id}
                                        value={categoria.id}
                                    >
                                        {categoria.nombre}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="form-field">
                            <span>Dificultad *</span>

                            <select
                                value={dificultad}
                                onChange={event => setDificultad(event.target.value)}
                                required
                            >
                                <option value="">Selecciona…</option>

                                {DIFICULTADES.map(valor => (
                                    <option key={valor} value={valor}>
                                        {valor}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="form-field">
                            <span>Autor</span>

                            <input
                                type="text"
                                value={autor}
                                onChange={event => setAutor(event.target.value)}
                            />
                        </label>

                    </div>

                    <div className="form-medidas">

                        <label className="form-field">
                            <span>Alto (cm)</span>

                            <input
                                type="number"
                                min="0"
                                step="0.1"
                                value={altura}
                                onChange={event => setAltura(event.target.value)}
                            />
                        </label>

                        <label className="form-field">
                            <span>Ancho (cm)</span>

                            <input
                                type="number"
                                min="0"
                                step="0.1"
                                value={ancho}
                                onChange={event => setAncho(event.target.value)}
                            />
                        </label>

                        <label className="form-field">
                            <span>Peso (g)</span>

                            <input
                                type="number"
                                min="0"
                                step="0.1"
                                value={peso}
                                onChange={event => setPeso(event.target.value)}
                            />
                        </label>

                    </div>

                    <label className="form-field">
                        <span>Descripción</span>

                        <textarea
                            rows={4}
                            value={descripcion}
                            onChange={event => setDescripcion(event.target.value)}
                        />
                    </label>

                    <div className="form-field" ref={comboColoresRef}>
                        <span>Colores *</span>

                        {errorOpciones && (
                            <p className="form-status form-status--error">
                                {errorOpciones}
                            </p>
                        )}

                        <div className="form-combo">

                            <button
                                type="button"
                                className="form-combo__control"
                                onClick={() =>
                                    setComboColoresAbierto(prev => !prev)
                                }
                                disabled={cargandoOpciones}
                            >
                                {coloresSeleccionados.length === 0 ? (
                                    <span className="form-combo__placeholder">
                                        {cargandoOpciones
                                            ? "Cargando colores…"
                                            : "Selecciona colores…"}
                                    </span>
                                ) : (
                                    <span className="form-combo__tags">
                                        {coloresSeleccionados.map(color => (
                                            <span
                                                className="form-combo__tag"
                                                key={color.id}
                                            >
                                                <span
                                                    className="form-combo__tag-swatch"
                                                    style={{
                                                        backgroundColor: color.codigo
                                                    }}
                                                />

                                                {color.nombre}

                                                <span
                                                    role="button"
                                                    tabIndex={-1}
                                                    className="form-combo__tag-quitar"
                                                    onClick={event => {
                                                        event.stopPropagation();
                                                        toggleColor(color.id);
                                                    }}
                                                >
                                                    ×
                                                </span>
                                            </span>
                                        ))}
                                    </span>
                                )}

                                <span className="form-combo__flecha">
                                    {comboColoresAbierto ? "▲" : "▼"}
                                </span>
                            </button>

                            {comboColoresAbierto && (
                                <div className="form-combo__panel">

                                    <input
                                        type="text"
                                        className="form-combo__buscador"
                                        placeholder="Buscar color…"
                                        value={filtroColor}
                                        onChange={event =>
                                            setFiltroColor(event.target.value)
                                        }
                                        autoFocus
                                    />

                                    <div className="form-combo__lista">

                                        {coloresOrdenados.map(color => (
                                            <label
                                                className={
                                                    "form-combo__opcion" +
                                                    (
                                                        coloresIds.includes(color.id)
                                                            ? " form-combo__opcion--activa"
                                                            : ""
                                                    )
                                                }
                                                key={color.id}
                                            >
                                                <input
                                                    type="checkbox"
                                                    checked={coloresIds.includes(color.id)}
                                                    onChange={() => toggleColor(color.id)}
                                                />

                                                <span
                                                    className="form-combo__opcion-swatch"
                                                    style={{
                                                        backgroundColor: color.codigo
                                                    }}
                                                />

                                                {color.nombre}
                                            </label>
                                        ))}

                                        {coloresOrdenados.length === 0 && (
                                            <p className="form-combo__vacio">
                                                Sin resultados.
                                            </p>
                                        )}

                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <label className="form-field">
                        <span>
                            {esEdicion
                                ? "Cambiar imagen principal"
                                : "Imagen principal *"}
                        </span>

                        <input
                            type="file"
                            accept="image/*"
                            onChange={handleImagenPrincipal}
                            required={!esEdicion}
                        />
                    </label>

                    {imagenPrincipalPreview && (
                        <img
                            className="form-preview form-preview--principal"
                            src={imagenPrincipalPreview}
                            alt="Vista previa imagen principal"
                        />
                    )}

                    <label className="form-field">
                        <span>Imágenes secundarias</span>

                        <input
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handleImagenesSecundarias}
                        />
                    </label>

                    {imagenesSecundariasPreview.length > 0 && (
                        <div className="form-preview-grid">
                            {imagenesSecundariasPreview.map((src, index) => (
                                <img
                                    key={index}
                                    className="form-preview"
                                    src={src}
                                    alt={`Vista previa secundaria ${index + 1}`}
                                />
                            ))}
                        </div>
                    )}

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
                                : "Crear figura"}
                    </button>

                </form>
            </main>

            <Footer />
        </div>
    );
}

export default FiguraForm;