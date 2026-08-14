

import "./AdminFigurasTable.css";

// ImageKit: tr=w-<ancho>,h-<alto>,fo-auto,q-<calidad>
function getImagenOptimizada(url, sizeCss) {
    if (!url) return url;
    const dpr = Math.min(window.devicePixelRatio || 1, 2); // cap en 2x
    const size = Math.round(sizeCss * dpr);
    const separador = url.includes("?") ? "&" : "?";
    return `${url}${separador}tr=w-${size},h-${size},fo-auto,q-70`;
}

function AdminFigurasTable({
    figuras,
    onEditar,
    onEliminar
}) {


    return (
        <div className="admin-tabla-container">

            <table className="admin-tabla">

                <thead>
                    <tr>
                        <th>Imagen</th>
                        <th>Nombre</th>
                        <th>Categoria</th>
                        <th>Acciones</th>
                    </tr>
                </thead>

                <tbody>

                    {figuras.map(figura => {

                        return (
                            <tr key={figura.id}>
                                <td>

                                    <img
                                        src={getImagenOptimizada(figura.imagenPrincipal, 60)}
                                        alt={figura.nombre}
                                        className="admin-tabla__imagen"
                                        loading="lazy"
                                    />

                                </td>
                                <td>{figura.nombre}</td>
                                <td>{figura.categoria}</td>
                                <td>
                                    <div className="admin-tabla__acciones">

                                        <button className="btn-editar"
                                            onClick={() => onEditar(figura.id)}
                                        >
                                            Editar
                                        </button>

                                        <button className="btn-eliminar"
                                            onClick={() => onEliminar(figura.id)}
                                        >
                                            Eliminar
                                        </button>

                                    </div>
                                </td>

                            </tr>
                        );

                    })}
                </tbody>
            </table>
        </div>
    );
}

export default AdminFigurasTable;